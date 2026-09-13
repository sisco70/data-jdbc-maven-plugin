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

public class MySqlTypeMapper extends AbstractTypeMapper {

    public MySqlTypeMapper(String timestampTzClassName, String jsonNodePackage, String catalog, String schemaPattern) {
        super(timestampTzClassName, jsonNodePackage, catalog, schemaPattern);
    }

    @Override
    public String mapSqlType(int type, String typeName, int precision, int scale) {
        String name = (typeName == null) ? "" : typeName.toLowerCase();

        return switch (type) {
            case Types.TINYINT -> {
                if (precision == 1 || name.contains("bool")) yield "java.lang.Boolean";
                yield "java.lang.Integer";
            }
            case Types.SMALLINT -> "java.lang.Integer";
            case Types.INTEGER -> {
                if (name.contains("unsigned")) yield "java.lang.Long";
                yield "java.lang.Integer";
            }
            case Types.BIGINT -> {
                if (name.contains("unsigned")) {
                    yield "java.math.BigInteger";
                }
                yield "java.lang.Long";
            }

            case Types.DECIMAL, Types.NUMERIC -> "java.math.BigDecimal";
            case Types.FLOAT, Types.REAL -> "java.lang.Float";
            case Types.DOUBLE -> "java.lang.Double";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.CLOB, Types.NVARCHAR, Types.NCHAR -> "java.lang.String";
            case Types.BOOLEAN -> "java.lang.Boolean";
            case Types.BIT -> {
                if (precision > 1) yield "byte[]";
                yield "java.lang.Boolean";
            }

            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME -> "java.time.LocalTime";
            case Types.TIMESTAMP -> {
                if (name.contains("timestamp")) yield timestampTzClassName;
                yield "java.time.LocalDateTime";
            }

            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "byte[]";

            case Types.OTHER, Types.JAVA_OBJECT -> switch (name) {
                case "json" -> jsonNodePackage;
                case "year" -> "java.lang.Integer";
                case "geometry", "point", "linestring", "polygon", 
                     "multipoint", "multilinestring", "multipolygon", 
                     "geometrycollection" -> "byte[]";
                
                default -> {
                    if (name.contains("json")) yield jsonNodePackage;
                    if (name.contains("enum") || name.contains("set")) yield "java.lang.String";
                    yield "java.lang.String";
                }
            };

            default -> {
                if (name.contains("json")) yield jsonNodePackage;
                if (name.contains("year")) yield "java.lang.Integer";
                if (name.contains("enum") || name.contains("set")) yield "java.lang.String";
                log.warn("Unsupported MySQL/MariaDB type: {} (name: {}), falling back to Object", type, name);
                yield "java.lang.Object";
            }
        };
    }
}