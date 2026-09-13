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

public class PostgresTypeMapper extends AbstractTypeMapper {

    public PostgresTypeMapper(String timestampTzClassName, String jsonNodePackage, String catalog, String schemaPattern) {
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


            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.CLOB -> "java.lang.String";
            case Types.BOOLEAN, Types.BIT -> "java.lang.Boolean";

            case Types.TIMESTAMP_WITH_TIMEZONE -> timestampTzClassName;
            case Types.TIMESTAMP -> name.contains("tz") ? timestampTzClassName : "java.time.LocalDateTime";
            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> "java.time.LocalTime";

            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "byte[]";

            case Types.ARRAY -> switch (name) {
                case "_int2", "_int4" -> "java.util.List<Integer>";
                case "_int8" -> "java.util.List<Long>";
                case "_float4", "_float8" -> "java.util.List<Double>";
                case "_varchar", "_text", "_bpchar", "_name" -> "java.util.List<String>";
                case "_bool" -> "java.util.List<Boolean>";
                case "_uuid" -> "java.util.List<java.util.UUID>";
                case "_point", "_line", "_lseg", "_box", "_path", "_polygon", "_circle",
                     "_geometry", "_geography" -> "java.util.List<byte[]>";
                
                default -> "java.util.List<Object>";
            };

            case Types.OTHER -> switch (name) {
                case "jsonb", "json" -> jsonNodePackage;
                case "uuid" -> "java.util.UUID";
                case "point", "line", "lseg", "box", "path", "polygon", "circle",
                     "geometry", "geography" -> "byte[]";

                default -> "java.lang.String";
            };

            default -> {
                if (name.contains("json")) yield jsonNodePackage;
                log.warn("Unsupported Postgres type: {} (name: {}), falling back to Object", type, name);
                yield "java.lang.Object";
            }
        };
    }
}