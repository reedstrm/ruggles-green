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

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * A file name filter that can also filter by directory/file type.
 * 
 * @author tal
 */
public class FileFilter implements FilenameFilter {

    public static enum KindFilter {
        FILES_ONLY,
        DIRECTORIES_ONLY,
        ALL;
    }

    @Nullable
    private final Pattern pattern;

    @Nullable
    private final KindFilter kindFilter;

    public FileFilter(@Nullable String regex, @Nullable KindFilter kindFilter) {
        this.pattern = (regex == null) ? null : Pattern.compile(regex);
        this.kindFilter = kindFilter;
    }

    @Override
    public boolean accept(File dir, String childName) {
        // Filter by optional name pattern
        if (pattern != null && !pattern.matcher(childName).matches()) {
            return false;
        }

        // Filter by optional kind filter
        if (kindFilter == null  || kindFilter == KindFilter.ALL) {
            return true;
        }

        switch (kindFilter) {
            case FILES_ONLY:
                return new File(dir, childName).isFile();
            case DIRECTORIES_ONLY:
                return new File(dir, childName).isDirectory();
            default:
                throw new RuntimeException("Unknown KindFilter value: " + kindFilter);
        }
    }
}
