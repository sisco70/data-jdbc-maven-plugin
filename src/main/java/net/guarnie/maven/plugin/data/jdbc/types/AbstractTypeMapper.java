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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTypeMapper {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final String timestampTzClassName;
    protected final String jsonNodePackage;
    protected final String catalog;
    protected final String schemaPattern;

    public AbstractTypeMapper(String timestampTzClassName, String jsonNodePackage, String catalog, String schemaPattern) {
        this.timestampTzClassName = timestampTzClassName;
        this.jsonNodePackage = jsonNodePackage;
        this.catalog = catalog;
        this.schemaPattern = schemaPattern;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public String getCatalog() { return catalog; }

    public abstract String mapSqlType(int type, String typeName, int precision, int scale);
}