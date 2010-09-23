/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.qi4j.library.sql.postgresql.internal;

import java.sql.Connection;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.library.sql.common.EntityTypeInfo;
import org.qi4j.library.sql.common.QNameInfo;

/**
 * The state-type interface containing some important database-related data, in order to create proper SQL statements in
 * indexing ({@link SQLIndexing}), querying ({@link SQLQuery}) and parsing queries ({@link SQLQueryParser}), and application startup ({@link SQLStartup}.
 *
 * @author Stanislav Muhametsin
 */
public interface PostgreSQLDBState
{
   /**
    * The schema name where all the required tables are located.
    * @return The schema name where all the required tables are located.
    */
   @Optional
   Property<String> schemaName();
   
   /**
    * Information about all used qualified names.
    * @return Information about all used qualified names.
    * @see QNameInfo
    */
   @Optional
   Property<Map<QualifiedName, QNameInfo>> qNameInfos();
   
   /**
    * Information about all used qualified names in a certain entity type. The interface name of entity type serves as the key.
    * @return Information about all used qualified names in a certain entity type.
    */
   @Optional
   Property<Map<String, Set<QualifiedName>>> entityUsedQNames();
   
   /**
    * Information about next primary keys for all used tables. Table name is the key. Each primary key needs to be specifically kept cached like this,
    * because it is quite damn hard, if not impossible, to use auto-generated keys with batch-updates (and batch-updates are very efficient when committing
    * changed entity states to DB).
    * @return Information about next primary keys for all used tables.
    */
   @Optional
   Property<Map<String, Long>> tablePKs();
   
   /**
    * Primary keys of all used classes (of value composites) in all entity types. Value composite type name (interface name) is the key.
    * @return Primary keys of all used classes (of value composites) in all entity types.
    */
   @Optional
   Property<Map<String, Integer>> usedClassesPKs();
   
   /**
    * Information about each used entity type. Entity type name (interface name) is the key.
    * @return Information about each used entity type.
    */
   @Optional
   Property<Map<String, EntityTypeInfo>> entityTypeInfos();
   
   /**
    * The connection to the database.
    * @return The connection to the database.
    */
   @Optional
   Property<Connection> connection();
   
   /**
    * A mapping between java type and the ones in {@link Types}. The class of java type is the key.
    * @return A mapping between java type and the ones in {@link Types}.
    */
   @Optional
   Property<Map<Class<?>, Integer>> javaTypes2SQLTypes();
   
   @Optional
   Property<Map<String, Integer>> enumPKs();
}