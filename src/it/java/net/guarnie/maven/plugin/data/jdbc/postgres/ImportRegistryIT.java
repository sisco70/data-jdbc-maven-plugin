package net.guarnie.maven.plugin.data.jdbc.postgres;

import net.guarnie.maven.plugin.data.jdbc.CompileHelper;
import net.guarnie.maven.plugin.data.jdbc.QueryHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the global {@code importRegistry} of mappings.yml: a registry class is imported only by the
 * records that really use it in a column type or in an annotation.
 */
class ImportRegistryIT extends AbstractPostgresIntegrationTest {
    private static final String GENERATED_DIR = GENERATED_PACKAGE_NAME.replace('.', '/');
    private static final String FIXTURE = "net.guarnie.maven.plugin.data.jdbc.fixture.";
    private static final String RECORD_KIND = FIXTURE + "RecordKind";
    private static final String MARKER = FIXTURE + "Marker";
    private static final String OTHER_MARKER = FIXTURE + "other.Marker";
    private static final String TIME_UNIT = "java.util.concurrent.TimeUnit";

    private String generated(String className) throws Exception {
        Path file = outputDir.resolve(GENERATED_DIR + "/" + className + ".java");
        assertTrue(Files.exists(file), "The record for '%s' was not generated!".formatted(className));
        return Files.readString(file);
    }

    private static void assertImported(String source, String fqcn) {
        assertTrue(source.contains("import " + fqcn + ";"), "Missing import of " + fqcn + " in:\n" + source);
    }

    private static void assertNotImported(String source, String fqcn) {
        assertFalse(source.contains("import " + fqcn + ";"), "Unexpected import of " + fqcn + " in:\n" + source);
    }

    @Test
    void importsRegistryClassesOnlyWhereUsed() throws Exception {
        executeMojo("""
                importRegistry:
                  - "%s"
                  - "%s"
                  - "%s"
                filters:
                  include: ["table_one", "table_two"]
                tables:
                  table_one:
                    name: "FirstTable"
                    columns:
                      type:
                        type: "RecordKind"
                      flag:
                        annotations: ["@Marker"]
                  table_two:
                    name: "SecondTable"
                """.formatted(RECORD_KIND, MARKER, TIME_UNIT));

        String first = generated("FirstTable");
        assertImported(first, RECORD_KIND);
        assertImported(first, MARKER);
        assertNotImported(first, TIME_UNIT);

        String second = generated("SecondTable");
        assertNotImported(second, RECORD_KIND);
        assertNotImported(second, MARKER);
        assertNotImported(second, TIME_UNIT);

        assertTrue(CompileHelper.compile(outputDir), "The generated Java files must compile without errors!");

        executeRecordQueryTest("FirstTable",
                "SELECT * FROM table_one WHERE id = ?",
                List.of(1),
                (results, recordClass) -> {
                    assertFalse(results.isEmpty(), "The query must return at least one record");
                    Object kind = QueryHelper.get(results.get(0), "type");
                    assertEquals("SYSTEM_USER", kind.toString());
                    assertEquals(RECORD_KIND, kind.getClass().getName());
                }
        );
    }

    @Test
    void importsClassesUsedInsideAnnotationArguments() throws Exception {
        executeMojo("""
                importRegistry: ["%s", "%s", "%s"]
                filters:
                  include: ["table_one"]
                tables:
                  table_one:
                    name: "FirstTable"
                    columns:
                      flag:
                        annotations: ['@Marker(note = "TimeUnit", kind = RecordKind.OTHER)']
                """.formatted(RECORD_KIND, MARKER, TIME_UNIT));

        String first = generated("FirstTable");
        assertImported(first, MARKER);
        assertImported(first, RECORD_KIND);
        // The text inside a string literal is not a class reference
        assertNotImported(first, TIME_UNIT);

        assertTrue(CompileHelper.compile(outputDir), "The generated Java files must compile without errors!");
    }

    @Test
    void qualifiedNamesDoNotTriggerRegistryImports() throws Exception {
        executeMojo("""
                importRegistry: ["%s"]
                filters:
                  include: ["table_one"]
                tables:
                  table_one:
                    name: "FirstTable"
                    columns:
                      flag:
                        annotations: ["@%s"]
                """.formatted(MARKER, OTHER_MARKER));

        String first = generated("FirstTable");
        assertNotImported(first, MARKER);
        assertTrue(first.contains("@" + OTHER_MARKER), "The qualified annotation must be kept as written");

        assertTrue(CompileHelper.compile(outputDir), "The generated Java files must compile without errors!");
    }

    @Test
    void tableImportWinsOverRegistryOnSameSimpleName() throws Exception {
        executeMojo("""
                importRegistry: ["%s"]
                filters:
                  include: ["table_one"]
                tables:
                  table_one:
                    name: "FirstTable"
                    imports: ["%s"]
                    columns:
                      flag:
                        annotations: ["@Marker"]
                """.formatted(MARKER, OTHER_MARKER));

        String first = generated("FirstTable");
        assertImported(first, OTHER_MARKER);
        assertNotImported(first, MARKER);

        assertTrue(CompileHelper.compile(outputDir), "The generated Java files must compile without errors!");
    }

    @Test
    void rejectsTwoRegistryClassesWithSameSimpleName() {
        MojoExecutionException e = assertThrows(MojoExecutionException.class, () -> executeMojo("""
                importRegistry: ["%s", "%s"]
                """.formatted(MARKER, OTHER_MARKER)));

        Throwable cause = e;
        boolean found = false;
        while (cause != null && !found) {
            found = cause.getMessage() != null && cause.getMessage().contains("same simple name");
            cause = cause.getCause();
        }
        assertTrue(found, "The error must explain that two registry classes share the same simple name");
    }

    @Test
    void rejectsUnqualifiedRegistryEntries() {
        assertThrows(MojoExecutionException.class, () -> executeMojo("""
                importRegistry: ["RecordKind"]
                """));
    }
}
