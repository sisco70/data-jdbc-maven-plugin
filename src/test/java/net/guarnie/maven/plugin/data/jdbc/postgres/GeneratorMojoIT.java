package net.guarnie.maven.plugin.data.jdbc.postgres;

import net.guarnie.maven.plugin.data.jdbc.CompileHelper;
import net.guarnie.maven.plugin.data.jdbc.QueryHelper;
import net.guarnie.maven.plugin.data.jdbc.GeneratorMojo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


class GeneratorMojoIT extends AbstractPostgresIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(GeneratorMojoIT.class);
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
    private static final String TABLE_NAME_1 = "FirstTable";
    private static final String TABLE_NAME_2 = "SecondTable";
    private static final String TABLE_NAME_3 = "TableThree";
    private static final String TABLE_NAME_4 = "TableFour";

    private static final String GENERATED_DIR = GENERATED_PACKAGE_NAME.replace('.', '/');
    private static final String MAPPINGS_FILE = "mappings.yaml";


    private void executeMojo(String mappingsYaml) throws Exception {
        Path mappingFile = tempDir.resolve(MAPPINGS_FILE);
        Files.writeString(mappingFile, mappingsYaml);

        GeneratorMojo mojo = new GeneratorMojo();
        mojo.setEnvPath(envFile);
        mojo.setPackageName(GENERATED_PACKAGE_NAME);
        mojo.setOutputPath(outputDir);
        mojo.setMappingsPath(mappingFile);

        mojo.execute();
    }

    @Test
    void testRecordFiles() throws Exception {
        String mappings = """
                filters:
                  exclude: ["flyway_schema_*"]
                  include: ["table_one", "table_two", "table_three", "table_four"]
                tables:
                  table_one:
                    name: "%s"
                  table_two:
                    name: "%s"
                  table_three:
                    subPackage: "test"
                    columns:
                      doc:
                        name: "document"
                  table_four:
                    imports: ["com.fasterxml.jackson.annotation.JsonIgnore"]
                    columns:
                      scores:
                        annotations: ["@JsonIgnore"]
                """.formatted(TABLE_NAME_1, TABLE_NAME_2);

        executeMojo(mappings);

        Path generatedFile = outputDir.resolve(GENERATED_DIR + "/" + TABLE_NAME_1 + ".java");
        assertTrue(Files.exists(generatedFile), "The record for '%s' was not generated!".formatted(TABLE_NAME_1));

        log.info("\n\n-------------------- RECORD GENERATED: {} -----------------------\n{}", TABLE_NAME_1, Files.readString(generatedFile));

        generatedFile = outputDir.resolve(GENERATED_DIR + "/" + TABLE_NAME_2 + ".java");
        assertTrue(Files.exists(generatedFile), "The record for '%s' was not generated!".formatted(TABLE_NAME_2));

        log.info("\n\n-------------------- RECORD GENERATED: {} -----------------------\n{}", TABLE_NAME_2, Files.readString(generatedFile));

        generatedFile = outputDir.resolve(GENERATED_DIR + "/test/" + TABLE_NAME_3 + ".java");
        assertTrue(Files.exists(generatedFile), "The record for '%s' was not generated!".formatted(TABLE_NAME_3));

        log.info("\n\n-------------------- RECORD GENERATED: {} -----------------------\n{}", TABLE_NAME_3, Files.readString(generatedFile));

        generatedFile = outputDir.resolve(GENERATED_DIR + "/" + TABLE_NAME_4 + ".java");
        assertTrue(Files.exists(generatedFile), "The record for '%s' was not generated!".formatted(TABLE_NAME_4));

        log.info("\n\n-------------------- RECORD GENERATED: {} -----------------------\n{}", TABLE_NAME_4, Files.readString(generatedFile));

        generatedFile = outputDir.resolve(GENERATED_DIR + "/tableToBeIgnored.java");
        assertFalse(Files.exists(generatedFile), "The record for 'tableToBeIgnored.java' must not be generated!");

        generatedFile = outputDir.resolve(GENERATED_DIR + "/flywaySchemaHistory.java");
        assertFalse(Files.exists(generatedFile), "The record for 'flywaySchemaHistory.java' must not be generated!");

        boolean isCompiled = CompileHelper.compile(outputDir);
        assertTrue(isCompiled, "The generated Java files must compile without errors!");

        log.info("\n\n-------------------- ALL RECORD COMPILED -----------------------");

        executeRecordQueryTest(TABLE_NAME_1,
                "SELECT * FROM table_one WHERE id = ?",
                List.of(1),
                (results, recordClass) -> {
                    assertFalse(results.isEmpty(), "The query must return at least one record");

                    Object record = results.get(0);

                    assertEquals("SYSTEM_USER", QueryHelper.get(record, "type"));
                    assertTrue((boolean) QueryHelper.get(record, "flag"));
                }
        );

        executeRecordQueryTest(TABLE_NAME_2,
                "SELECT * FROM table_two WHERE code = ?",
                List.of(404),
                (results, recordClass) -> {
                    assertFalse(results.isEmpty(), "The query must return at least one record");

                    Object record = results.get(0);
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse("9999-12-31 01:00:00+01", dtFormatter);

                    assertEquals(offsetDateTime.toInstant(), QueryHelper.get(record, "date_end"));
                }
        );

        /*executeRecordQueryTest(TABLE_NAME_3,
                "SELECT * FROM table_three WHERE code = ?",
                List.of(404),
                (results, recordClass) -> {
                    assertFalse(results.isEmpty(), "The query must return at least one record");

                    Object record = results.get(0);

                    assertEquals("SYSTEM_USER", QueryHelper.get(record, "json_txt"));
                    assertTrue((boolean) QueryHelper.get(record, "flag"));
                }
        );

        executeRecordQueryTest(TABLE_NAME_4,
                "SELECT * FROM table_four WHERE id = ?",
                List.of(1),
                (results, recordClass) -> {
                    assertFalse(results.isEmpty(), "The query must return at least one record");

                    Object record = results.get(0);

                    assertEquals("SYSTEM_USER", QueryHelper.get(record, "type"));
                    assertTrue((boolean) QueryHelper.get(record, "flag"));
                }
        );*/
    }

}