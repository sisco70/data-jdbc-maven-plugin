# CHANGELOG

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
