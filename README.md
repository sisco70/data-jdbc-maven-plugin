# data-jdbc-maven-plugin


[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/net.guarnie/data-jdbc-maven-plugin.svg)](https://search.maven.org/artifact/TUO_GROUP_ID/TUO_ARTIFACT_ID)
![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)

---

A Maven plugin designed to generate Java Records for use with Spring Data JDBC, reverse-engineered directly from a database schema.

### Example of usage within a pom.xml using default parameters

- Environment variables: Database credentials loaded from a .env file located in the project root.

- Built-in template: Uses the standard **table-record.hbs** template bundled with the plugin.
- Default mappings: Automatically processes all tables within the specified schema.

```xml
<plugin>
    <groupId>net.guarnie</groupId>
    <artifactId>data-jdbc-maven-plugin</artifactId>
    <version>0.1.1</version>
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
- To map TIMESTAMP fields with TIMEZONE use OffsetDateTime instead of Instant (default=true):
```xml
<useOffsetDateTime>true</useOffsetDateTime>
```
- Include swagger annotations that use field and table descriptions (default=true):
```xml
<useSwagger>true</useSwagger>
```
- Includes Jakarta Validation annotations that refer to fields and tables (default=true):
```xml
<useJakartaValidation>true</useJakartaValidation>
```
- Indicates whether Jackson 3 should be used for JSON processing (default=true):
```xml
<useJackson3>true</useJackson3>
```
<br>

#### Custom Database Configuration File


Example .env file for database access:
```properties
jdbc.driver=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:5417/atms
jdbc.schema=master

jdbc.user=master
jdbc.pass=*******
```

<br>

#### Custom Mappings File

By default, the plugin automatically processes database schemas to generate Java records and attributes following Spring Data JDBC naming conventions (converting tables to PascalCase and columns to camelCase, while stripping underscores).  
Through the mapping.yml file, you can fully customize the generation pipeline with the following capabilities:  

- **Regex-Based Table Filtering**: Supports fine-grained schema filtering using regular expressions.  
  You can explicitly define include patterns to restrict generation to specific tables, and exclude patterns to bypass system, log, or temporary tables (e.g., preventing Flyway history or temporary backups from being processed).
- **Granular Naming Overrides**: Explicitly define target names for specific records or attributes, bypassing default naming rules.
- **Custom Package Routing**: Supplement the global packageName configured in the Maven plugin (pom.xml) by defining table-specific subPackage properties to logically organize your generated records.
- **Annotation Injection**: Programmatically attach custom Java annotations to specific attributes.
- **Dynamic Component Imports**: Declare custom Java imports at the record level to cleanly resolve third-party framework dependencies (such as Jackson or validation constraints).

Example of mappings.yml file:
```yaml
filters:
  include: ["auth_.*"]
  exclude: ["flyway_schema_history", "temp_.*"]

tables:
  role_scopes:
    subPackage: "roles"
    imports: ["com.fasterxml.jackson.annotation.JsonIgnore"]
    columns:
      role_desc:
        annotations: ["@JsonIgnore"]
  auth_users: 
    name: "AllUsers"
    columns:
      email_addr: 
        name: "emailAddress"
        annotations: ["@JsonIgnore"]
```


