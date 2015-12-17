/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.api.structure;

import java.util.stream.Stream;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.value.ValueDescriptor;

/**
 * Module Descriptor.
 */
public interface ModuleDescriptor
{
    String name();

    LayerDescriptor layer();

    /**
     * @return the Module's ClassLoader
     */
    ClassLoader classLoader();

    /**
     * @param typeName name of a transient composite type
     *
     * @return the descriptor for a transient composite or null if the class could not be found or the transient composite is not visible
     */
    TransientDescriptor transientDescriptor( String typeName );

    /**
     * @param typeName name of an entity composite type
     *
     * @return the descriptor for an entity composite or null if the class could not be found or the entity composite is not visible
     */
    EntityDescriptor entityDescriptor( String typeName );

    /**
     * @param typeName name of an object type
     *
     * @return the descriptor for an object or null if the class could not be found or the object is not visible
     */
    ObjectDescriptor objectDescriptor( String typeName );

    /**
     * @param typeName name of a value composite type
     *
     * @return the descriptor for a value composite or null if the class could not be found or the value composite is not visible
     */
    ValueDescriptor valueDescriptor( String typeName );

    Stream<? extends TransientDescriptor> findVisibleTransientTypes();

    Stream<? extends ValueDescriptor> findVisibleValueTypes();

    Stream<? extends EntityDescriptor> findVisibleEntityTypes();

    Stream<? extends ObjectDescriptor> findVisibleObjectTypes();

    Stream<? extends TransientDescriptor> transientComposites();

    Stream<? extends ValueDescriptor> valueComposites();

    Stream<? extends EntityDescriptor> entityComposites();

    Stream<? extends ObjectDescriptor> objects();

    Stream<? extends ImportedServiceDescriptor> importedServices();

    Stream<? extends ServiceDescriptor> serviceComposites();

    Module instance();

    TypeLookup typeLookup();
}
