package net.guarnie.maven.plugin.data.jdbc;




import org.apache.commons.lang3.tuple.Pair;
import java.util.*;
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

    /**
     * Mappings configuration for custom table and column names.
     */
    private MappingsConfig mappings ;

    /**
     * Default constructor initializing filter and mappings configurations.
     */
    public GeneratorMappings() {
        this.filters = new FiltersConfig();
        this.mappings = new MappingsConfig();
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
     * Gets the mappings configuration.
     * @return MappingsConfig
     */
    public MappingsConfig getMappings() { return mappings; }

    /**
     * Sets the mappings configuration.
     * @param mappings MappingsConfig instance
     */
    public void setMappings(MappingsConfig mappings) { this.mappings = mappings; }


    /**
     * Controls whether the record for the parameter should be generated
     * @param tableName Table name
     * @return True Whether the record for the table name should be generated
     */
    public boolean shouldProcessTable(String tableName) {
        return filters.isIncluded(tableName) && !filters.isExcluded(tableName);
    }

    /**
     * Returns the generated name of the table (pascal case) or the custom name coming from the mappings.yml file
     * @param tableName Table name
     * @return The generated name of the table (pascal case) or the custom name
     */
    public String getMappedTableName(String tableName) {
        return mappings.getTables().getOrDefault(tableName, toPascalCase(tableName));
    }

    /**
     * The generated name of the column (camel case) or the custom name
     * @param tableName Table name
     * @param columnName Column name
     * @return The generated name of the column (camel case) or the custom name
     */
    public Pair<String, Boolean> getMappedColumnName(String tableName, String columnName) {
        String name = mappings.getColumnsForTable(tableName).get(columnName);
        boolean custom = name != null;
        return Pair.of(custom? name : toCamelCase(columnName), custom);
    }

    /**
     * Core logic to transform a snake_case string to PascalCase.
     * @param s String to transform
     * @return StringBuilder with the PascalCase representation
     */
    private StringBuilder transformCase(String s) {
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
    private String toPascalCase(String s) {
        return transformCase(s).toString();
    }

    /**
     * Transforms the string to camelCase
     * @param s String to transform
     * @return String in camelCase
     */
    private String toCamelCase(String s) {
        StringBuilder sb = transformCase(s);
        if (!sb.isEmpty()) sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Internal class that represents the associations between real names in the database and custom names
     */
    public static class MappingsConfig {
        /**
         * Custom table name mappings: real_name -> custom_name.
         */
        private Map<String, String> tables;
        /**
         * Custom column name mappings: table_name -> (real_column_name -> custom_column_name).
         */
        private Map<String, Map<String, String>> columns;

        /**
         * Default constructor
         */
        public MappingsConfig() {
            this.tables = new HashMap<>();
            this.columns =  new HashMap<>();
        }

        /**
         * Returns the map of configured tables
         * @return Map (real name -> custom name)
         */
        public Map<String, String> getTables() { return tables; }

        /**
         * Sets the table map
         * @param t Table map
         */
        public void setTables(Map<String, String> t) { this.tables = t; }

        /**
         * Returns the map of configured columns per table
         * @return Map (table name -> map (real column name -> custom column name))
         */
        public Map<String, Map<String, String>> getColumns() { return columns; }

        /**
         * Sets the column map
         * @param c Column map
         */
        public void setColumns(Map<String, Map<String, String>> c) { this.columns = c; }

        /**
         * Returns the column mappings for a specific table
         * @param tableName Table name
         * @return Column map for the table (empty if not present)
         */
        public Map<String, String> getColumnsForTable(String tableName) {
            return columns.getOrDefault(tableName, Map.of());
        }
    }

    /**
     * Internal class representing filter settings
     */
    public static class FiltersConfig {
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
}