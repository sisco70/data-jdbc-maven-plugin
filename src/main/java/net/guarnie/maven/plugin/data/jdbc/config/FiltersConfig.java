/*
 * Copyright © 2026 Francesco Guarnieri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.guarnie.maven.plugin.data.jdbc.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Internal class representing filter settings
 */
public class FiltersConfig {
    /**
     * Compiled patterns for including tables.
     */
    private List<Pattern> includePatterns;

    /**
     * Compiled patterns for excluding tables.
     */
    private List<Pattern> excludePatterns;

    /**
     * Default constructor
     */
    public FiltersConfig() {
        this.includePatterns = List.of();
        this.excludePatterns = List.of();
    }

    /**
     * Sets the inclusion patterns. If the list contains ".*" or is empty, everything is included.
     * @param include List of regex patterns
     */
    public void setInclude(List<String> include) {
        if (include == null || include.isEmpty() || include.contains(".*")) this.includePatterns = List.of();
        else {
            List<Pattern> compiled = new ArrayList<>(include.size());
            for (String s : include) if (s != null && !s.isBlank()) compiled.add(Pattern.compile(s));
            this.includePatterns = List.copyOf(compiled);
        }
    }

    /**
     * Sets the exclusion patterns.
     * @param exclude List of regex patterns
     */
    public void setExclude(List<String> exclude) {
        if (exclude == null || exclude.isEmpty()) this.excludePatterns = List.of();
        else {
            List<Pattern> compiled = new ArrayList<>(exclude.size());
            for (String s : exclude) if (s != null && !s.isBlank()) compiled.add(Pattern.compile(s));
            this.excludePatterns = List.copyOf(compiled);
        }
    }

    /**
     * Checks if the table name is included in the filters.
     * @param t Table name
     * @return true if included or if there are no inclusion filters
     */
    public boolean isIncluded(String t) {
        if (includePatterns.isEmpty()) return true;
        for (Pattern p : includePatterns) if (p.matcher(t).matches()) return true;
        return false;
    }

    /**
     * Checks if the table name is excluded from the filters.
     * @param t Table name
     * @return true if excluded
     */
    public boolean isExcluded(String t) {
        if (excludePatterns.isEmpty()) return false;
        for (Pattern p : excludePatterns) if (p.matcher(t).matches()) return true;
        return false;
    }
}