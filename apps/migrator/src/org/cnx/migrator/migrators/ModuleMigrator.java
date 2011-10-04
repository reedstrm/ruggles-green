/*
 * Copyright (C) 2011 The CNX Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cnx.migrator.migrators;

import static com.google.common.base.Preconditions.checkArgument;
import static org.cnx.migrator.util.MigratorUtil.checkAtombuyEntryId;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.cnx.migrator.context.MigratorContext;
import org.cnx.migrator.io.Directory;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.util.MigratorUtil;
import org.cnx.repository.atompub.CnxAtomPubUtils;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.resourcemapping.LocationInformation;
import org.cnx.resourcemapping.ObjectFactory;
import org.cnx.resourcemapping.Repository;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;

import com.google.common.collect.ImmutableList;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * A migrator for a module item, including all of its versions
 * 
 * @author tal
 */
public class ModuleMigrator extends ItemMigrator {

    /** The root directory of the module to migrate. */
    private final Directory moduleDirectory;

    /** Module id in the repository. E.g. "m0012". */
    private final String cnxModuleId;

    /**
     * Construct a single module migrator.
     * 
     * The constructor should not do any significant amount of work. This is done later by the
     * {@link #doWork()} method.
     * 
     * @param context the context of this migration session.
     * @param moduleDirectory root data directory of the module to migrate. Its base name represent
     *            the numeric value of the module id (e.g. "000012").
     */
    public ModuleMigrator(MigratorContext context, Directory moduleDirectory) {
        super(context);
        this.moduleDirectory = moduleDirectory;
        this.cnxModuleId = moduleDirectoryToId(moduleDirectory);
    }

    /** This is called by a worker thread to do the migration */
    @Override
    public void doWork() {

        ClientEntry atompubEntry = createModule();

        int nextVersionNum = 1;

        // NOTE(tal): Version directories have sequential numeric names starting from 1 and
        // their lexicographic order preserves the numeric order using zero padding. Some
        // versions may be missing due to take down.
        final ImmutableList<Directory> versionDirectories = moduleDirectory.getSubDirectories();
        // TODO(tal): decide what we want to do with these modules, if any
        checkArgument(versionDirectories.size() > 0, "Module has no versions: %s", moduleDirectory);

        for (Directory versionDirectory : versionDirectories) {
            final int directoryVersionNum = Integer.parseInt(versionDirectory.getName());
            checkArgument(directoryVersionNum >= nextVersionNum, "%s", versionDirectory);

            // If needed, create gap versions
            while (directoryVersionNum > nextVersionNum) {
                MigratorUtil.sleep(getConfig().getTransactionDelayMillis());
                getContext().incrementCounter("MODULE_VERSION_TAKEDOWNS", 1);
                // TODO(tal): create gaps as explicit taken down version.
                Log.message("** Creating gap module version: %s/%s", cnxModuleId, nextVersionNum);
                migrateNextModuleVersion(atompubEntry, nextVersionNum, versionDirectory);
                nextVersionNum++;
            }

            // Create the actual version
            MigratorUtil.sleep(getConfig().getTransactionDelayMillis());
            migrateNextModuleVersion(atompubEntry, nextVersionNum, versionDirectory);
            nextVersionNum++;
        }
    }

    /** Map module data directory to CNX module id in the repository. */
    private static String moduleDirectoryToId(Directory moduleDirectory) {
        final String directoryName = moduleDirectory.getName(); // e.g. "0000012"
        final int directoryNumber = Integer.valueOf(directoryName); // e.g. 12
        // MOTE(tal): This matches the repository module key to id mapping
        return String.format("m%04d", directoryNumber); // e.g. "m0012"
    }

    /**
     * Create the module entity in the repository. Module versions are migrated later.
     * 
     * @returns the atompub entry to use to upload the first module version.
     */
    private ClientEntry createModule() {
        getContext().incrementCounter("MODULES", 1);
        Log.message("Going to migrate module: %s", cnxModuleId);
        int attempt;
        for (attempt = 1;; attempt++) {
            final ClientEntry atompubEntry;
            IdWrapper cnxModuleIdWrapper = new IdWrapper(cnxModuleId, IdWrapper.Type.MODULE);
            try {
                atompubEntry = getCnxClient().createNewModuleForMigration(cnxModuleIdWrapper);
                checkAtombuyEntryId(cnxModuleId, 1, atompubEntry);
                message("Added module: %s", atompubEntry.getId());
                return atompubEntry;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("MODULES_WITH_CREATION_RETRIES", 1);
                }
                getContext().incrementCounter("MODULE_CREATION_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s",
                            attempt, cnxModuleId), e);
                }
                Log.message("**** Attempt %d failed to write module %s. Will retry", attempt,
                        cnxModuleId);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
            }
        }
    }

    /**
     * Migrate next version of this module.
     * <p>
     * The method assumes that the module has been created and that exactly all the version prior to
     * this version have already been migrated.
     * <p>
     * NOTE(tal): for now we also use this to create gap versions so versionNum may be higher than
     * the versionDirectory numeric value.
     * 
     * @param atompubEntry the atompub entry to use for posting this module version. Upon return,
     *            this entry is modified so it can be used to upload the next version of this
     *            module.
     * @param versionNum version number (1 based) of this new version.
     * @param versionDirectory root directory of this module version data.
     */
    private void migrateNextModuleVersion(ClientEntry atompubEntry, int versionNum,
            Directory versionDirectory) {
        getContext().incrementCounter("MODULE_VERSIONS", 1);
        final String resourceMapXml = readAndConstructResourceMapXML(versionDirectory);

        int attempt;
        for (attempt = 1;; attempt++) {
            try {
                // TODO(tal): *** implement module version mapping from major.minor to number
                // in the xml file
                final String cnxml = versionDirectory.readXmlFile("index.cnxml");

                // TODO(tal): upload resource map from property file
                getCnxClient().createNewModuleVersion(atompubEntry, cnxml, resourceMapXml);
                checkAtombuyEntryId(cnxModuleId, versionNum, atompubEntry);
                // NOTE(tal): here atompubEntry.getEditURI points to a URL to post the next version.

                Log.message("Migrated module version %s/%s", cnxModuleId, versionNum);
                return;
            } catch (Exception e) {
                if (attempt == 1) {
                    getContext().incrementCounter("MODULE_VERSIONS_WITH_UPLOAD_RETRIES", 1);
                }
                getContext().incrementCounter("MODULE_VERSTION_UPLOAD_RETRIES", 1);
                if (attempt >= getConfig().getMaxAttempts()) {
                    throw new RuntimeException(String.format("Failed after %d attempts: %s/%s",
                            attempt, cnxModuleId, versionNum), e);
                }
                Log.message("**** Attempt %d failed to write module version %s/%s. Will retry",
                        attempt, cnxModuleId, versionNum);
                MigratorUtil.sleep(getConfig().getFailureDelayMillis());
                // NOTE(tal): If got an exception, atompubEntry is guaranteed to not be changed.
            }
        }
    }

    /**
     * Read and construct resource map XML doc.
     * 
     * @param vers      ionDirectory the root data directory of this module version.
     * 
     *            TODO(tal): simplify CnxAtomPubClient.getResourceMappingFromResourceEntries() so it
     *            does not use atompub entires, etc and and share logic with this one.
     */
    private String readAndConstructResourceMapXML(Directory versionDirectory) {
        final HierarchicalINIConfiguration resourcesInfo =
                versionDirectory.readIniFile("resources");
        try {
            ObjectFactory objectFactory = new ObjectFactory();
            Resources resources = objectFactory.createResources();

            final BigDecimal RESOURCE_MAPPING_DOC_VERSION = new BigDecimal(1.0);
            resources.setVersion(RESOURCE_MAPPING_DOC_VERSION);

            List<Resource> list = resources.getResource();
            for (Object sectionNameObject : resourcesInfo.getSections()) {
                final String sectionName = (String) sectionNameObject;
                checkArgument("resource".equals(sectionName), "[%s]", sectionName);
                final SubnodeConfiguration section = resourcesInfo.getSection(sectionName);
                final Resource resourceFromEntry = objectFactory.createResource();
                list.add(resourceFromEntry);

                resourceFromEntry.setName(section.getString("filename"));

                final String REPOSITORY_ID = "cnx-repo";
                Repository repository = objectFactory.createRepository();
                repository.setRepositoryId(REPOSITORY_ID);

                final int resourceIdNum = section.getInt("fileid");
                final String resourceId = String.format("r%04d", resourceIdNum);
                repository.setResourceId(resourceId);

                LocationInformation locationInformation = objectFactory.createLocationInformation();
                locationInformation.setRepository(repository);

                resourceFromEntry.setLocationInformation(locationInformation);
            }
            return CnxAtomPubUtils.jaxbObjectToString(Resources.class, resources);
        } catch (Exception e) {
            throw new RuntimeException("At module version: " + versionDirectory, e);
        }
    }
}
