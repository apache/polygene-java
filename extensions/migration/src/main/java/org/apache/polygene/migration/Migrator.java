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
 *
 *
 */
package org.apache.polygene.migration;

import java.util.Map;
import javax.json.JsonObject;
import org.apache.polygene.migration.assembly.MigrationContext;

/**
 * The Migrator implements this interface, which is invoked by MigrationOperation implementations
 * to perform changes to EntityState during a version migration.
 */
public interface Migrator
{
    JsonObject addProperty( MigrationContext content, JsonObject state,
                            String name, Object defaultValue );

    JsonObject removeProperty( MigrationContext content, JsonObject state,
                               String name );

    JsonObject renameProperty( MigrationContext content, JsonObject state,
                               String from, String to );

    JsonObject addAssociation( MigrationContext content, JsonObject state,
                               String name, String defaultReference );

    JsonObject removeAssociation( MigrationContext content, JsonObject state,
                                  String name );

    JsonObject renameAssociation( MigrationContext content, JsonObject state,
                                  String from, String to );

    JsonObject addManyAssociation( MigrationContext content, JsonObject state,
                                   String name, String... defaultReferences );

    JsonObject removeManyAssociation( MigrationContext content, JsonObject state,
                                      String name );

    JsonObject renameManyAssociation( MigrationContext content, JsonObject state,
                                      String from, String to );

    JsonObject addNamedAssociation( MigrationContext content, JsonObject state,
                                    String name, Map<String, String> defaultReferences );

    JsonObject removeNamedAssociation( MigrationContext content, JsonObject state,
                                       String name );

    JsonObject renameNamedAssociation( MigrationContext content, JsonObject state,
                                       String from, String to );

    JsonObject changeEntityType( MigrationContext content, JsonObject state,
                                 String fromType, String toType );
}
