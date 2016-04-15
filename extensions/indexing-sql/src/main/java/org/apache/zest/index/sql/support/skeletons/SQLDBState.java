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
package org.apache.zest.index.sql.support.skeletons;

import java.sql.Types;
import java.util.Map;
import java.util.Set;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.property.Property;
import org.apache.zest.index.sql.support.api.SQLIndexing;
import org.apache.zest.index.sql.support.common.QNameInfo;

/**
 * The state-type interface containing some important database-related data, in order to create
 * proper SQL statements in indexing ({@link SQLIndexing}), querying (
 * {@link org.apache.zest.index.sql.support.api.SQLQuerying}) and application startup (
 * {@link org.apache.zest.index.sql.support.api.SQLAppStartup}.
 */
public interface SQLDBState
{
    /**
     * The schema name where all the required tables are located.
     *
     * @return The schema name where all the required tables are located.
     */
    @Optional
    Property<String> schemaName();

    /**
     * Information about all used qualified names.
     *
     * @return Information about all used qualified names.
     * @see QNameInfo
     */
    @Optional
    Property<Map<QualifiedName, QNameInfo>> qNameInfos();

    /**
     * Information about all used qualified names in a certain entity type. The interface name of
     * entity type serves as the key.
     *
     * @return Information about all used qualified names in a certain entity type.
     */
    @Optional
    Property<Map<EntityDescriptor, Set<QualifiedName>>> entityUsedQNames();

    /**
     * Primary keys of all used composites in all entities of this application. (Value) Composite
     * descriptor is the key.
     *
     * @return Primary keys of all used classes (of value composites) in all entity types.
     */
    @Optional
    Property<Map<CompositeDescriptor, Integer>> usedClassesPKs();

    @Optional
    Property<Map<String, Integer>> entityTypePKs();

    /**
     * A mapping between java type and the ones in {@link Types}. The class of java type is the key.
     *
     * @return A mapping between java type and the ones in {@link Types}.
     */
    @Optional
    Property<Map<Class<?>, Integer>> javaTypes2SQLTypes();

    @Optional
    Property<Map<String, Integer>> enumPKs();

}
