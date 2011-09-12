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

package org.cnx.migrator.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.cnx.migrator.util.MigratorUtil;

import com.google.common.collect.ImmutableList;

/**
 * An abstraction of a directory in the CNX input data structure.
 * 
 * @author tal
 */
public class Directory {

    private final File dir;

    public Directory(File dir) {
        this.dir = checkNotNull(dir);
        checkArgument(dir.isDirectory(), "Not a directory: %s", dir);
    }

    public File getDir() {
        return dir;
    }

    public Directory subDirectory(String name) {
        return new Directory(new File(dir, name));
    }

    public File subFile(String name) {
        final File result = new File(dir, name);
        checkArgument(result.isFile(), "Not a file: %s", result);
        return result;
    }

    /** Return list of sub directories */
    public ImmutableList<Directory> getSubDirectories() {
        final FilenameFilter filter = new FileFilter(null, null);
        String[] list = list(filter);
        ImmutableList.Builder<Directory> builder = new ImmutableList.Builder<Directory>();
        for (String name : list) {
            builder.add(subDirectory(name));
        }
        return builder.build();
    }

    /** A wrapper around dir.list() that sorts the result */
    private String[] list(FilenameFilter filter) {
        final String[] result = dir.list(filter);
        Arrays.sort(result);
        return result;
    }

    @Override
    public String toString() {
        return dir.getAbsolutePath().toString();
    }

    /** Return the content of a property file within this directory */
    public Properties readPropertiesFile(String fileName) {
        InputStream in = null;
        try {
            final File file = subFile(fileName);
            in = new FileInputStream(file);
            final Properties result = new Properties();
            result.load(in);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            MigratorUtil.safeClose(in);
        }
    }
}
