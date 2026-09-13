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
package net.guarnie.maven.plugin.data.jdbc.types;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeMapperFactory {
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^jdbc:([^:]+):", Pattern.CASE_INSENSITIVE);
    private static final String JDBC_URL = "jdbc.url";
    private static final String JDBC_USER = "jdbc.user";
    private static final String JDBC_PASS = "jdbc.pass";
    private static final String JDBC_SCHEMA = "jdbc.schema";

    public record FactoryResult(
            AbstractTypeMapper mapper,
            Properties properties,
            String dbUrl
    ) {}

    public static FactoryResult create(Properties dbEnv, String timestampTzClassName, String jsonNodePackage) {
        Properties connProps = new Properties();
        connProps.setProperty("user", dbEnv.getProperty(JDBC_USER));
        connProps.setProperty("password", dbEnv.getProperty(JDBC_PASS));
        String schemaPattern = dbEnv.getProperty(JDBC_SCHEMA);
        String dbUrl = dbEnv.getProperty(JDBC_URL);
        if (dbUrl == null || dbUrl.isBlank()) throw new IllegalArgumentException("The JDBC URL (dbUrl) is a required parameter");

        Matcher matcher = PROTOCOL_PATTERN.matcher(dbUrl);
        String protocol = matcher.find()? matcher.group(1).toLowerCase() : "generic";

        String catalog = null;
        AbstractTypeMapper mapper;

        switch (protocol) {
            case "oracle" -> {
                connProps.setProperty("remarksReporting", "true");
                mapper = new OracleTypeMapper(timestampTzClassName, jsonNodePackage, catalog, schemaPattern == null? null : schemaPattern.toUpperCase());
            }
            case "postgresql", "postgres" -> mapper = new PostgresTypeMapper(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
            case "sqlserver" -> mapper = new SqlServerTypeMapper(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
            case "mysql", "mariadb" -> {
                catalog = schemaPattern;
                schemaPattern = null;
                connProps.setProperty("useInformationSchema", "true");
                mapper = new MySqlTypeMapper(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
            }
            default -> mapper = new GenericTypeMapper(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
        }

        return new FactoryResult(mapper, connProps, dbUrl);
    }
}