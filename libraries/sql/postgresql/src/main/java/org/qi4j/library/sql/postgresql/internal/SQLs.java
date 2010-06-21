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

/**
 * This is helper class to contain all of most of the SQL constants used throughout the SQL Indexing.
 *
 * @author Stanislav Muhametsin
 *
 */
public class SQLs
{

   public static final String QNAME_TABLE_NAME_PREFIX = "qname_";

   public static final String QNAME_TABLE_VALUE_COLUMN_NAME = "qname_value";

   public static final String QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_NAME = "asso_index";

   public static final String QNAME_TABLE_ASSOCIATION_INDEX_COLUMN_DATA_TYPE = "INTEGER";

   public static final String QNAME_TABLE_PARENT_QNAME_COLUMN_NAME = "parent_qname";

   public static final String QNAME_TABLE_COLLECTION_PATH_COLUMN_NAME = "collection_path";

   public static final String QNAME_TABLE_COLLECTION_PATH_COLUMN_DATA_TYPE = "ltree";

   public static final String QNAME_TABLE_COLLECTION_PATH_TOP_LEVEL_NAME = "Top";

   public static final String QNAME_TABLE_COLLECTION_PATH_SEPARATOR = ".";

   public static final String ALL_QNAMES_TABLE_NAME = "all_qnames";

   public static final String ALL_QNAMES_TABLE_PK_COLUMN_NAME = "qname_id";

   public static final String ALL_QNAMES_TABLE_PK_COLUMN_DATA_TYPE = "INTEGER";

//   public static final String QNAME_TABLE_COLLECTION_INDEX_COLUMN_NAME_PREFIX = "index_";
//
//   public static final String QNAME_TABLE_COLLECTION_INDEX_COLUMN_DATA_TYPE = "INTEGER";

   public static final String USED_CLASSES_TABLE_NAME = "used_classes";

   public static final String USED_CLASSES_TABLE_PK_COLUMN_NAME = "used_class_id";

   public static final String USED_CLASSES_TABLE_PK_COLUMN_DATA_TYPE = "INTEGER";

   public static final String USED_CLASSES_TABLE_CLASS_NAME_COLUMN_NAME = "class_name";

   public static final String USED_CLASSES_TABLE_CLASS_NAME_COLUMN_DATA_TYPE = "VARCHAR(1024)";

   public static final String USED_QNAMES_TABLE_NAME = "used_qnames";

   public static final String USED_QNAMES_TABLE_QNAME_COLUMN_NAME = "qname";

   public static final String USED_QNAMES_TABLE_QNAME_COLUMN_DATA_TYPE = "VARCHAR(10240)";

   public static final String USED_QNAMES_TABLE_TABLE_NAME_COLUMN_NAME = "table_name";

   public static final String USED_QNAMES_TABLE_TABLE_NAME_COLUMN_DATA_TYPE = "VARCHAR(512)";

   public static final String ENTITY_TYPES_TABLE_PK_COLUMN_NAME = "entity_type_id";

   public static final String ENTITY_TYPES_TABLE_PK_COLUMN_DATA_TYPE = "INTEGER";

   public static final String ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_NAME = "entity_type_name";

   public static final String ENTITY_TYPES_TABLE_TYPE_NAME_COLUMN_DATA_TYPE = "VARCHAR(1024)";

   public static final String ENTITY_TYPES_TABLE_NAME = "entity_types";

   public static final String ENTITY_TABLE_NAME = "entities";

   public static final String ENTITY_TABLE_PK_COLUMN_NAME = "entity_pk";

   public static final String ENTITY_TABLE_PK_COLUMN_DATA_TYPE = "BIGINT";

   public static final String ENTITY_TABLE_IDENTITY_COLUMN_NAME = "entity_identity";

   public static final String ENTITY_TABLE_IDENTITY_COLUMN_DATA_TYPE = "VARCHAR(1024)";

   public static final String ENTITY_TABLE_MODIFIED_COLUMN_NAME = "modified";

   public static final String ENTITY_TABLE_MODIFIED_COLUMN_DATA_TYPE = "TIMESTAMP WITH TIME ZONE";

   public static final String ENTITY_TABLE_VERSION_COLUMN_NAME = "entity_version";

   public static final String ENTITY_TABLE_VERSION_COLUMN_DATA_TYPE = "VARCHAR(1024)";

   public static final String ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME = "application_version";

   public static final String ENTITY_TABLE_APPLICATION_VERSION_COLUMN_DATATYPE = "VARCHAR(256)";

   public static final String APP_VERSION_TABLE_NAME = "app_version";

   public static final String APP_VERSION_PK_COLUMN_NAME = "app_version";

   public static final String APP_VERSION_PK_COLUMN_DATA_TYPE = "VARCHAR(1024)";

   public static final String ENUM_LOOKUP_TABLE_NAME = "enum_lookup";

   public static final String ENUM_LOOKUP_TABLE_PK_COLUMN_NAME = "enum_id";

   public static final String ENUM_LOOKUP_TABLE_PK_COLUMN_DATA_TYPE = "INTEGER";

   public static final String ENUM_LOOKUP_TABLE_ENUM_VALUE_NAME = "enum_value";

   public static final String ENUM_LOOKUP_TABLE_ENUM_VALUE_DATA_TYPE = "VARCHAR(1024)";

   public static final String UPDATE_ENTITY_TABLE = //
      "UPDATE " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
      "SET " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = ?, " + //
      ENTITY_TABLE_MODIFIED_COLUMN_NAME + " = ?, " + //
      ENTITY_TABLE_VERSION_COLUMN_NAME + " = ?, " + //
      ENTITY_TABLE_APPLICATION_VERSION_COLUMN_NAME + " = ?" + "\n" + //
      "WHERE " + ENTITY_TABLE_PK_COLUMN_NAME + " = ?" + "\n" + //
      ";" //
      ;

   public static final String DELETE_FROM_ENTITY_TABLE = //
      "DELETE FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
      "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = ? " + "\n" + //
      ";" //
      ;

   public static final String QUERY_ENTITY_PK_BY_IDENTITY = //
      "SELECT " + ENTITY_TABLE_PK_COLUMN_NAME + "\n" + //
      "FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
      "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = ?" + "\n" + //
      ";" //
      ;

   public static final String CLEAR_ENTITY_DATA = //
      "DELETE FROM " + "%s" + "." + "%s" + "\n" + //
      "WHERE " + ENTITY_TABLE_PK_COLUMN_NAME + " = ?" + "\n" + //
      ";" //
      ;

   public static final String ONE_VALUE_SELECT = //
      "SELECT %s" + "\n" + //
      "FROM  %s.%s" + "\n" //
      ;

   public static final String TWO_VALUE_SELECT = //
      "SELECT %s, %s" + "\n" + //
      "FROM  %s.%s" + "\n" //
      ;

   public static final String TWO_VALUE_INSERT = //
      "INSERT INTO " + "%s" + "." + "%s" + " VALUES(" + "\n" + //
      "?, ?" + "\n" + //
      ");" //
      ;

   public static final String THREE_VALUE_INSERT = //
      "INSERT INTO " + "%s" + "." + "%s" + " VALUES(" + "\n" + //
      "?, ?, ?" + "\n" + //
      ");" //
      ;
   public static final String FOUR_VALUE_INSERT = //
      "INSERT INTO " + "%s" + "." + "%s" + " VALUES(" + "\n" + //
      "?, ?, ?, ?" + "\n" + //
      ");" //
      ;

   public static final String FIVE_VALUE_INSERT = //
      "INSERT INTO " + "%s" + "." + "%s" + " VALUES(" + "\n" + //
      "?, ?, ?, ?, ?" + "\n" + //
      ");" //
      ;

   public static final String SIX_VALUE_INSERT = //
      "INSERT INTO " + "%s" + "." + "%s" + " VALUES(" + "\n" + //
      "?, ?, ?, ?, ?, ?" + "\n" + //
      ");" //
      ;

   public static final String ASSO_INSERT = //
       "INSERT INTO " + "%s" + "." + "%s" + "\n" + //
       "SELECT " + "?, " + "?, " + ENTITY_TABLE_PK_COLUMN_NAME + "\n" + //
       "FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
       "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = " + "?";

   public static final String MANY_ASSO_INSERT = //
       "INSERT INTO " + "%s" + "." + "%s" + "\n" + //
       "SELECT " + "?, " + "?, " + "?, " + ENTITY_TABLE_PK_COLUMN_NAME + "\n" + //
       "FROM " + "%s" + "." + ENTITY_TABLE_NAME + "\n" + //
       "WHERE " + ENTITY_TABLE_IDENTITY_COLUMN_NAME + " = " + "?";

}
