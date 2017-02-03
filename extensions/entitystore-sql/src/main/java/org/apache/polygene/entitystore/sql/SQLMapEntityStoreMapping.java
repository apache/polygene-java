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
package org.apache.polygene.entitystore.sql;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

/**
 * Mapping for the entities table.
 *
 * If you change this once the table is created you'll have to ALTER it yourself.
 */
public interface SQLMapEntityStoreMapping
{
    default String defaultSchemaName()
    {
        return "POLYGENE_ES";
    }

    default String tableName()
    {
        return "POLYGENE_ENTITIES";
    }

    default String identityColumnName()
    {
        return "ENTITY_IDENTITY";
    }

    default String versionColumnName()
    {
        return "ENTITY_VERSION";
    }

    default String stateColumnName()
    {
        return "ENTITY_STATE";
    }

    default DataType<String> identityDataType()
    {
        return SQLDataType.VARCHAR( 64 );
    }

    default DataType<String> versionDataType()
    {
        return SQLDataType.VARCHAR( 64 );
    }

    default DataType<String> stateDataType()
    {
        return SQLDataType.VARCHAR( 10 * 1024 );
    }
}
