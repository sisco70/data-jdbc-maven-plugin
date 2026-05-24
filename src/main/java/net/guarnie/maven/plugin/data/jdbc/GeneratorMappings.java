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
package net.guarnie.maven.plugin.data.jdbc;


import net.guarnie.maven.plugin.data.jdbc.config.FiltersConfig;
import net.guarnie.maven.plugin.data.jdbc.config.TableConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * Bean used by snakeyaml to represent the contents of mappings.yml
 */
@SuppressWarnings("unused")
public class GeneratorMappings {
    /**
     * Filter configuration for including or excluding tables.
     */
    private FiltersConfig filters;


    private Map<String, TableConfig> tables;

    /**
     * Default constructor initializing filter and mappings configurations.
     */
    public GeneratorMappings() {
        this.filters = new FiltersConfig();
        this.tables = new HashMap<>();
    }

    /**
     * Gets the filter configuration.
     * @return FiltersConfig
     */
    public FiltersConfig getFilters() { return filters; }

    /**
     * Sets the filter configuration.
     * @param filters FiltersConfig instance
     */
    public void setFilters(FiltersConfig filters) { this.filters = filters; }

    /**
     * Retrieves the table configuration mappings.
     * This method returns a map where the key is the name of the table
     * and the value is the corresponding {@link TableConfig} containing
     * the configuration for that table.
     *
     * @return a map of table names to their respective configurations.
     */
    public Map<String, TableConfig> getTables() { return tables; }

    /**
     * Sets the table configuration mappings for the generator.
     * The provided map associates table names with their corresponding {@link TableConfig} objects.
     * The {@code TableConfig::update} method is called for each entry in the map to ensure
     * the configuration is up-to-date.
     *
     * @param t a map where the keys are table names and the values are {@link TableConfig} objects
     */
    public void setTables(Map<String, TableConfig> t) {
        this.tables = t;
        if (t != null) t.forEach(TableConfig::update);
    }


    public TableConfig getTableConfig(String tableName) {
        return tables.getOrDefault(tableName, new TableConfig(tableName));
    }

    /**
     * Determines whether the specified table should be processed based on inclusion and exclusion filters.
     * A table is processed if it is included in the filter configuration and not explicitly excluded.
     *
     * @param tableName the name of the table to evaluate
     * @return true if the table should be processed, false otherwise
     */
    public boolean shouldProcessTable(String tableName) {
        return filters.isIncluded(tableName) && !filters.isExcluded(tableName);
    }

    /**
     * Core logic to transform a snake_case string to PascalCase.
     * @param s String to transform
     * @return StringBuilder with the PascalCase representation
     */
    private static StringBuilder transformCase(String s) {
        if (s == null || s.isEmpty()) return new StringBuilder();
        int len = s.length();
        StringBuilder result = new StringBuilder(len);
        boolean nextUpper = true;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '_') nextUpper = true;
            else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else result.append(Character.toLowerCase(c));
        }
        return result;
    }

    /**
     * Transforms the string to PascalCase
     * @param s String to transform
     * @return String in PascalCase
     */
    public static String toPascalCase(String s) {
        return transformCase(s).toString();
    }

    /**
     * Transforms the string to camelCase
     * @param s String to transform
     * @return String in camelCase
     */
    public static String toCamelCase(String s) {
        StringBuilder sb = transformCase(s);
        if (!sb.isEmpty()) sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }
}