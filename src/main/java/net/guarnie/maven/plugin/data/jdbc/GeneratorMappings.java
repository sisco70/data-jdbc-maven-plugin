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


import net.guarnie.maven.plugin.data.jdbc.configs.FiltersConfig;
import net.guarnie.maven.plugin.data.jdbc.configs.TableConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
     * Matches string literals inside an annotation, so that their content is never mistaken for a class name.
     */
    private static final Pattern STRING_LITERAL = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");

    /**
     * Matches a simple (not qualified) Java identifier: not preceded by a dot or by another identifier character.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("(?<![\\w.$])[A-Za-z_$][\\w$]*");

    /**
     * Fully qualified class names declared in the global {@code importRegistry} section, as written in mappings.yml.
     */
    private List<String> importRegistry = List.of();

    /**
     * The same registry indexed by simple class name (derived from {@link #importRegistry}).
     */
    private Map<String, String> registryBySimpleName = Map.of();

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


    /**
     * Gets the global import registry: fully qualified names of classes (enums, annotations...) that can be
     * referenced by simple name from any column mapping.
     * @return the list of fully qualified class names
     */
    public List<String> getImportRegistry() { return importRegistry; }

    /**
     * Sets the global import registry. An entry is turned into an {@code import} statement only inside the records
     * that actually use its simple name (see {@link #findRegistryImports(String)}).
     *
     * @param fqcns fully qualified class names; blank entries are ignored
     * @throws IllegalArgumentException if an entry is not a qualified name or if two different classes
     *                                  share the same simple name
     */
    public void setImportRegistry(List<String> fqcns) {
        Map<String, String> bySimpleName = new LinkedHashMap<>();
        if (fqcns != null) {
            for (String entry : fqcns) {
                if (entry == null || entry.isBlank()) continue;
                String fqcn = entry.trim();
                int dotPos = fqcn.lastIndexOf('.');
                if (dotPos < 1 || dotPos == fqcn.length() - 1)
                    throw new IllegalArgumentException("Invalid class name in importRegistry (fully qualified name expected): " + fqcn);

                String simpleName = fqcn.substring(dotPos + 1);
                String previous = bySimpleName.putIfAbsent(simpleName, fqcn);
                if (previous != null && !previous.equals(fqcn))
                    throw new IllegalArgumentException("importRegistry contains two classes with the same simple name '%s': %s and %s"
                            .formatted(simpleName, previous, fqcn));
            }
        }
        this.importRegistry = List.copyOf(bySimpleName.values());
        this.registryBySimpleName = bySimpleName;
    }

    /**
     * Finds the registry classes referenced by a Java expression (a column type or an annotation, such as
     * {@code CodCarro} or {@code @References(Sede.class)}).
     * String literals and already qualified names ({@code a.b.Zone}) are ignored.
     *
     * @param expression the Java fragment to inspect
     * @return the fully qualified names of the registry classes used by the expression, in order of appearance
     */
    public Set<String> findRegistryImports(String expression) {
        if (registryBySimpleName.isEmpty() || expression == null || expression.isBlank()) return Set.of();

        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = IDENTIFIER.matcher(STRING_LITERAL.matcher(expression).replaceAll("\"\""));
        while (matcher.find()) {
            String fqcn = registryBySimpleName.get(matcher.group());
            if (fqcn != null) found.add(fqcn);
        }
        return found;
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