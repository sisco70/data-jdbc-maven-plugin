# CHANGELOG

### 0.3.0
- Added the global `importRegistry` list to mappings.yml: fully qualified classes (enums, annotations...) declared once and imported only
  in the records that reference them by simple name in a column `type` or in an annotation. Table-level `imports` still work and take precedence
  on a clash of simple names.
- Integration tests moved to `src/it` and run only with the `integration` profile (`mvn -Pintegration verify`), which now holds all
  the test dependencies; they share a single PostgreSQL container.
- Fixed the default template table-record.hbs: annotations are no longer HTML-escaped (e.g. `@Marker(note = "x")` was generated with `&#x3D;` and `&quot;`).
- Updated: handlebars version 4.5.8
- Updated build and test tooling: maven-plugin-tools 3.16.0, maven-compiler-plugin 3.16.0, maven-failsafe-plugin 3.6.1, build-helper-maven-plugin 3.7.0 (requires Maven 3.9+ to run the integration tests),
  flyway 13.13.0, postgresql driver 42.7.14, swagger-annotations 2.2.56, jackson-databind 3.3.0

### 0.2.1
- Added a new parameter `forceTableAnnotation`, which specifies whether to force the use of the @Table annotation even in the absence of a specific named mapping (default=false).

### 0.2.0
- Fixed `useJakartaValidation` flag behavior, which now disables the use of Jakarta Validation annotations.
- Revision of SQL-to-Java type mapping logic. Now more accurate and specialized per database:
  - Added support for Array data types in PostgreSQL and Oracle (basic)
  - Added support for Geospatial data types in PostgreSQL and MySQL/MariaDB
- Java type customization: Overrides the internal engine's Java type for an attribute (e.g., to map a custom Enum)
- Added a preliminary version of integration tests
- Updated: handlebars version 4.5.4
- Updated: snakeyaml version 2.7

### 0.1.2
- Minor fixes in default template table-record.hbs (table comment in record main javadoc)
- Updated: handlebars version 4.5.3,  maven-plugin-api version 3.9.16

### 0.1.1
- Added the ability to specify a subPackage for individual records, which appends to the main packageName configured in the Maven pom.xml.

### 0.1.0
- Changed mappings.yml to use the new naming convention.
  See example inside README.md
- Added support to custom annotations and imports
- Added support to Jackson 3.x

### 0.0.6
- minor fixes
- Don't use the @Size annotation when the size of a text field exceeds 32768 characters

### 0.0.5
- new pom.xml configuration `useSwagger`: Include swagger annotations that use field and table descriptions (default):
- new pom.xml configuration `useJakartaValidation`: Includes Jakarta Validation annotations that refer to fields and tables (default):
- updated dependencies versions

### 0.0.4
- ⚠️ License Change: Starting from version 0.0.4, this project is licensed under the Apache License 2.0.

### 0.0.3
- First release
