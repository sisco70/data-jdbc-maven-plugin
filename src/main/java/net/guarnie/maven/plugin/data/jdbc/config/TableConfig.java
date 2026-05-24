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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static net.guarnie.maven.plugin.data.jdbc.GeneratorMappings.toPascalCase;

/**
 * Represents the configuration of a table, including the table name
 * and column-specific customizations. This class provides methods to
 * set and retrieve the table name and column mappings.
 */
public class TableConfig {
    private boolean customName = false;
    private String name;

    /**
     * Represents a set of fully qualified Java import statements associated with a table configuration.
     */
    private Set<String> imports;

    /**
     * A mapping of column names to their respective configurations.
     * This map is used to define custom configurations for each column in a table,
     * where the key is the name of the column, and the value is the associated
     * {@link ColumnConfig} object that specifies the configuration details.
     */
    private Map<String, ColumnConfig> columns;


    public TableConfig() {
        this.imports = new TreeSet<>();
        this.columns =  new HashMap<>();
    }

    public TableConfig(String name) {
       this();
       this.name = toPascalCase(name);
    }

    /**
     * Retrieves the name of the table associated with this configuration.
     * @return the table name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the table for this configuration.
     *
     * @param name the name of the table to associate with this configuration
     */
    public void setName(String name) {
        customName = name != null && !name.isBlank();
        this.name = name;
    }

    /**
     * Updates the name property of the given {@code TableConfig} object if it is null.
     * The name is assigned using the PascalCase transformation of the provided key.
     *
     * @param key the reference key used to generate the name in PascalCase
     * @param tableConfig the {@code TableConfig} object to be updated; if null, no changes are made
     */
    public static void update(String key, TableConfig tableConfig) {
        if (tableConfig != null && tableConfig.name == null) tableConfig.name = toPascalCase(key);
    }

    /**
     * Returns the map of configured columns per table
     * @return Map (real column name -> custom column data)
     */
    public Map<String, ColumnConfig> getColumns() { return columns; }

    /**
     * Sets the column configuration for the table.
     * The provided map associates column names with their respective configurations.
     * For each entry in the map, the associated {@code ColumnConfig} object is updated.
     *
     * @param c a map where keys are column names and values are {@code ColumnConfig} objects
     */
    public void setColumns(Map<String, ColumnConfig> c) {
        this.columns = c;
        if (c != null) c.forEach(ColumnConfig::update);
    }

    /**
     * Retrieves the configuration details for a specific column. If the column
     * does not have an existing configuration, a new {@code ColumnConfig} object
     * is created with the provided column name.
     *
     * @param columnName the name of the column for which configuration details are to be fetched
     * @return the {@code ColumnConfig} object associated with the specified column name,
     *         or a new {@code ColumnConfig} initialized with the column name if none exists
     */
    public ColumnConfig getColumnConfig(String columnName) {
        return columns.getOrDefault(columnName, new ColumnConfig(columnName));
    }

    /**
     * Indicates whether the table configuration has a custom name set.
     *
     * @return true if a custom name has been assigned to the table, false otherwise
     */
    public boolean isCustomName() {
        return customName;
    }

    /**
     * Retrieves the set of import statements associated with the table configuration.
     * @return a set of strings representing the import statements.
     */
    public Set<String> getImports() {
        return imports;
    }

    /**
     * Sets the set of import statements associated with the table configuration.
     *
     * @param imports the set of import statements to be associated with this configuration
     */
    public void setImports(Set<String> imports) {
        this.imports = imports;
    }
}
