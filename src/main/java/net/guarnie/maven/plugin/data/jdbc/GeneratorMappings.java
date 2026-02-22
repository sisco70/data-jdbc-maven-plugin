package net.guarnie.maven.plugin.data.jdbc;




import org.apache.commons.lang3.tuple.Pair;
import java.util.*;
import java.util.regex.Pattern;



/**
 *
 */
@SuppressWarnings("unused")
public class GeneratorMappings {
    private FiltersConfig filters = new FiltersConfig();
    private MappingsConfig mappings = new MappingsConfig();

    public FiltersConfig getFilters() { return filters; }
    public void setFilters(FiltersConfig filters) { this.filters = filters; }
    public MappingsConfig getMappings() { return mappings; }
    public void setMappings(MappingsConfig mappings) { this.mappings = mappings; }


    public boolean shouldProcessTable(String tableName) {
        return filters.isIncluded(tableName) && !filters.isExcluded(tableName);
    }

    public String getMappedTableName(String tableName) {
        return mappings.getTables().getOrDefault(tableName, toPascalCase(tableName));
    }

    public Pair<String, Boolean> getMappedColumnName(String tableName, String columnName) {
        String name = mappings.getColumnsForTable(tableName).get(columnName);
        boolean custom = name != null;
        return Pair.of(custom? name : toCamelCase(columnName), custom);
    }

    /**
     * Trasforma in PascalCase (anche il carattere iniziale è maiuscolo)
     * @param s Stringa da trasformare
     * @return Stringa in Pascal case
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

    private String toPascalCase(String s) {
        return transformCase(s).toString();
    }

    private String toCamelCase(String s) {
        StringBuilder sb = transformCase(s);
        if (!sb.isEmpty()) sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    // Classi che contengono le informazioni del file yaml
    public static class MappingsConfig {
        private Map<String, String> tables = new HashMap<>();
        private Map<String, Map<String, String>> columns = new HashMap<>();
        public Map<String, String> getTables() { return tables; }
        public void setTables(Map<String, String> t) { this.tables = t; }
        public Map<String, Map<String, String>> getColumns() { return columns; }
        public void setColumns(Map<String, Map<String, String>> c) { this.columns = c; }

        public Map<String, String> getColumnsForTable(String tableName) {
            return columns.getOrDefault(tableName, Map.of());
        }
    }

    public static class FiltersConfig {
        private List<Pattern> includePatterns = List.of();
        private List<Pattern> excludePatterns = List.of();

        public void setInclude(List<String> include) {
            if (include == null || include.isEmpty() || include.contains(".*")) this.includePatterns = List.of();
            else {
                List<Pattern> compiled = new ArrayList<>(include.size());
                for (String s : include) if (s != null && !s.isBlank()) compiled.add(Pattern.compile(s));
                this.includePatterns = List.copyOf(compiled);
            }
        }

        public void setExclude(List<String> exclude) {
            if (exclude == null || exclude.isEmpty()) this.excludePatterns = List.of();
            else {
                List<Pattern> compiled = new ArrayList<>(exclude.size());
                for (String s : exclude) if (s != null && !s.isBlank()) compiled.add(Pattern.compile(s));
                this.excludePatterns = List.copyOf(compiled);
            }
        }

        public boolean isIncluded(String t) {
            if (includePatterns.isEmpty()) return true;
            for (Pattern p : includePatterns) if (p.matcher(t).matches()) return true;
            return false;
        }

        public boolean isExcluded(String t) {
            if (excludePatterns.isEmpty()) return false;
            for (Pattern p : excludePatterns) if (p.matcher(t).matches()) return true;
            return false;
        }
    }
}