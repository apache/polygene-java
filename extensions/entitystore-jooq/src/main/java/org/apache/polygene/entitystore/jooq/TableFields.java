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

import java.sql.Timestamp;
import org.jooq.Field;

import static org.apache.polygene.entitystore.jooq.TypesTable.makeField;

public interface TableFields
{
    // Common in all tables
    String IDENTITY_COLUMN_NAME = "identity";
    String CREATED_COLUMN_NAME = "created_at";
    String LASTMODIFIED_COLUMN_NAME = "modified_at";

    // Types Table
    String TABLENAME_COLUMN_NAME = "table_name";

    // Entities Table
    String VALUEID_COLUMN_NAME = "value_id";
    String TYPE_COLUMN_NAME = "type";
    String VERSION_COLUMN_NAME = "version";
    String APPLICATIONVERSION_COLUMN_NAME = "app_version";

    // Mixin Tables
    String NAME_COLUMN_NAME = "name";
    String INDEX_COLUMN_NAME = "index";    // either index in ManyAssociation or name in NamedAssociation
    String REFERENCE_COLUMN_NAME = "reference";
    String ASSOCS_TABLE_POSTFIX = "_ASSOCS";


    // Common Fields
    Field<String> identityColumn = makeField( IDENTITY_COLUMN_NAME, String.class );
    Field<Timestamp> createdColumn = makeField( CREATED_COLUMN_NAME, Timestamp.class );
    Field<Timestamp> modifiedColumn = makeField( LASTMODIFIED_COLUMN_NAME, Timestamp.class );

    // Types Table
    Field<String> tableNameColumn = makeField( TABLENAME_COLUMN_NAME, String.class );

    // Entities Table
    Field<String> valueIdentityColumn = makeField( VALUEID_COLUMN_NAME, String.class );
    Field<String> typeNameColumn = makeField( TYPE_COLUMN_NAME, String.class );
    Field<String> versionColumn = makeField( VERSION_COLUMN_NAME, String.class );
    Field<String> applicationVersionColumn = makeField( APPLICATIONVERSION_COLUMN_NAME, String.class );

    // Mixin Tables

    // The _ASSOCS table
    Field<String> nameColumn = makeField( NAME_COLUMN_NAME, String.class );
    Field<String> referenceColumn = makeField( REFERENCE_COLUMN_NAME, String.class );
    Field<String> indexColumn = makeField( INDEX_COLUMN_NAME, String.class );

}
