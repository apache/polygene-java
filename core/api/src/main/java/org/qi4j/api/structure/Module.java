/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman.
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
package org.qi4j.api.structure;

import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueDescriptor;

/**
 * API for interacting with a Module. Instances
 * of this can be accessed by using the {@link Structure}
 * injection scope.
 */
public interface Module
    extends ActivationEventListenerRegistration,
            MetaInfoHolder,
            ObjectFactory,
            TransientBuilderFactory,
            ValueBuilderFactory,
            UnitOfWorkFactory,
            QueryBuilderFactory,
            ServiceFinder
{

    /**
     * @return the Module's name
     */
    String name();

    /**
     * @return the Module's ClassLoader
     */
    ClassLoader classLoader();

    /**
     * @param typeName name of a transient composite type
     * @return the descriptor for a transient composite or null if the class could not be found or the transient composite is not visible
     */
    TransientDescriptor transientDescriptor( String typeName );

    /**
     * @param typeName name of an entity composite type
     * @return the descriptor for an entity composite or null if the class could not be found or the entity composite is not visible
     */
    EntityDescriptor entityDescriptor( String typeName );

    /**
     * @param typeName name of an object type
     * @return the descriptor for an object or null if the class could not be found or the object is not visible
     */
    ObjectDescriptor objectDescriptor( String typeName );

    /**
     * @param typeName name of a value composite type
     * @return the descriptor for a value composite or null if the class could not be found or the value composite is not visible
     */
    ValueDescriptor valueDescriptor( String typeName );
}
