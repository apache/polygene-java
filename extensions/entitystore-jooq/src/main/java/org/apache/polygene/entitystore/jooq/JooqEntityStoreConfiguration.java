/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.entitystore.jooq;

import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.library.sql.common.SQLConfiguration;

// START SNIPPET: config
public interface JooqEntityStoreConfiguration extends SQLConfiguration
{
    /**
     * Name of the database schema to use.
     * Ignored on SQL databases that don't support schemas.
     */
    @UseDefaults( "POLYGENE" )
    @Override
    Property<String> schemaName();

    /**
     * Name of the entities table.
     * <p>
     * This table contains the Identity and other metadata about each entity instance
     * </p>
     */
    @UseDefaults( "ENTITIES" )
    Property<String> entitiesTableName();

    /**
     * Name of the entity types table.
     * <p>
     * This table contains the metainfo about each type. Types are versioned according to
     * application version, to support entity migration over time, and therefor there might
     * be (but not necessarily) multiple tables for entity types that has evolved beyond
     * what can be managed within a single table.
     * </p>
     */
    @UseDefaults( "TYPES" )
    Property<String> typesTableName();

    /**
     * Defines whether the database schema and table should be created if not already present.
     */
    @UseDefaults( "true" )
    Property<Boolean> createIfMissing();

    /**
     * The SQL dialect that is being used.
     * <p>
     * Typically that is matching a supporting dialect in JOOQ.
     * See {@link org.jooq.SQLDialect} for supported values.
     * </p>
     * @return The property with the dialect value.
     */
    @UseDefaults( "" )
    Property<String> dialect();
}
// END SNIPPET: config
