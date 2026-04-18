# data-jdbc-maven-plugin


[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/net.guarnie/data-jdbc-maven-plugin.svg)](https://search.maven.org/artifact/TUO_GROUP_ID/TUO_ARTIFACT_ID)
![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)

---

A Maven plugin designed to generate Java Records for use with Spring Data JDBC, reverse-engineered directly from a database schema.

#### Example of usage within a pom.xml using default parameters

- Environment variables: Database credentials loaded from a .env file located in the project root.

- Built-in template: Uses the standard **table-record.hbs** template bundled with the plugin.
- Default mappings: Automatically processes all tables within the specified schema.

```xml
<plugin>
    <groupId>net.guarnie</groupId>
    <artifactId>data-jdbc-maven-plugin</artifactId>
    <version>0.0.5</version>
    <executions>
        <execution>
            <goals>
                <goal>generate-records</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <packageName>com.xyz.project.dao</packageName>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.2</version>
        </dependency>
    </dependencies>
</plugin>
```


The following customizations can be added inside the `configuration` block:
- Custom Database configuration path:
```xml
<envPath>${project.basedir}/config/.env</envPath>
```
- Custom mappings file path:
```xml
<mappingsPath>${project.basedir}/config/mappings.yml</mappingsPath>
```
- Custom templates directory path (to provide a custom version of **table-record.hbs**):
```xml
<templatesPath>${project.basedir}/config/templates</templatesPath>
```
- Custom output directory path (default: **${project.build.directory}/generated-sources/jdbc-records**):
```xml
<outputPath>${project.basedir}/out/records</outputPath>
```
- To map TIMESTAMP fields with TIMEZONE use OffsetDateTime instead of Instant (default):
```xml
<useOffsetDateTime>true</useOffsetDateTime>
```
- Include swagger annotations that use field and table descriptions (default):
```xml
<useSwagger>true</useSwagger>
```
- Includes Jakarta Validation annotations that refer to fields and tables (default):
```xml
<useJakartaValidation>true</useJakartaValidation>
```
#### Custom File Examples


Example .env file for database access:
```properties
jdbc.driver=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:5417/atms
jdbc.schema=master

jdbc.user=master
jdbc.pass=*******
```


Example mappings.yml file:
```yaml
filters:
  include: ["auth_.*"]
  exclude: ["flyway_schema_history", "temp_.*"]

mappings:
  tables:
    auth_users: "AllUsers"
  columns:
    auth_users:
      email: "emailAddress"
```

Both include and exclude are lists and support **regular expressions**.
- If `include` is not provided, all tables in the schema will be included by default.
- If `exclude` is not provided, no tables will be excluded.

