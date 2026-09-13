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

import java.sql.Types;

public class SqlServerTypeMapper extends AbstractTypeMapper {

    public SqlServerTypeMapper(String timestampTzClassName, String jsonNodePackage, String catalog, String schemaPattern) {
        super(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
    }

    @Override
    public String mapSqlType(int type, String typeName, int precision, int scale) {
        String name = (typeName == null) ? "" : typeName.toLowerCase();

        return switch (type) {
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> "java.lang.Integer";
            case Types.BIGINT -> "java.lang.Long";
            case Types.DECIMAL, Types.NUMERIC -> "java.math.BigDecimal";
            case Types.FLOAT, Types.REAL, Types.DOUBLE -> "java.lang.Double";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.CLOB, Types.NVARCHAR, Types.NCHAR ->
                    name.equals("uniqueidentifier") ? "java.util.UUID" : "java.lang.String";
            case Types.BOOLEAN, Types.BIT -> "java.lang.Boolean";

            case -155, Types.TIMESTAMP_WITH_TIMEZONE -> timestampTzClassName; 
            case Types.TIMESTAMP -> "java.time.LocalDateTime"; 
            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME -> "java.time.LocalTime";
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "byte[]";

            default -> {
                if (name.contains("json")) yield jsonNodePackage;
                log.warn("Unsupported SQL Server type: {} (name: {}), falling back to Object", type, name);
                yield "java.lang.Object";
            }
        };
    }
}