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
import static net.guarnie.maven.plugin.data.jdbc.GeneratorMappings.toCamelCase;

public class ColumnConfig {
    private boolean customName = false;
    private String name;
    private List<String> annotations;

    public ColumnConfig() {
        this.annotations = new ArrayList<>();
    }

    public ColumnConfig(String name) {
        this();
        this.name = toCamelCase(name);
    }

    /**
     * Retrieves the name associated with the column configuration.
     *
     * @return the name of the column configuration.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the column for this configuration.
     * Marks the column configuration as customized.
     * @param name the custom name to assign to the column
     */
    public void setName(String name) {
        customName = name != null && !name.isBlank();
        this.name = name;
    }

    /**
     * Updates the name property of the given {@code ColumnConfig} object if it is null.
     * The name is assigned based on the camelCase transformation of the provided key.
     *
     * @param key the reference key used to generate the name in camelCase
     * @param columnConfig the {@code ColumnConfig} object to be updated; if null, no changes are made
     */
    public static void update(String key, ColumnConfig columnConfig) {
        if (columnConfig != null && columnConfig.name == null) columnConfig.name = toCamelCase(key);
    }

    /**
     * Retrieves the list of annotations associated with the column configuration.
     *
     * @return a list of strings representing annotations for the column
     */
    public List<String> getAnnotations() {
        return annotations;
    }

    /**
     * Sets the list of annotations for this column configuration.
     *
     * @param annotations the list of annotation strings to associate with this column configuration
     */
    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    /**
     * Checks if a custom name has been set for the column configuration.
     *
     * @return true if a custom name has been set, false otherwise
     */
    public boolean isCustomName() {
        return customName;
    }
}