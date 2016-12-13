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
package org.apache.polygene.api.structure;

import org.apache.polygene.api.activation.ActivationEventListenerRegistration;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;

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
            QueryBuilderFactory,
            ServiceFinder
{

    /**
     * @return the Module's name
     */
    String name();

    ModuleDescriptor descriptor();

    /**
     * @return the Layer that the Module is declared in.
     */
    LayerDescriptor layer();

    /** Returns the TypeLookup for the Module.
     * TypeLookup handles all the types visible from within this Module.
     *
     * @return TypeLookup for this Module
     */
    TypeLookup typeLookup();

    /** Returns the UnitOfWorkFactory for this Module.
     *
     * @return the UnitOfWorkFactory of this Module.
     */
    UnitOfWorkFactory unitOfWorkFactory();

    /** Returns the ServiceFinder for this Module.
     *
     * @return the ServiceFinder for this Module.
     */
    ServiceFinder serviceFinder();

    /** Returns the ValueBuilderFactory for this Module.
     *
     * @return the ValueBuilderFactory for this Module.
     */
    ValueBuilderFactory valueBuilderFactory();

    /** Returns the TransientBuilderFactory for this Module.
     *
     * @return the TransientBuilderFactory for this Module.
     */
    TransientBuilderFactory transientBuilderFactory();

    /** Returns the ObjectFactory for this Module.
     *
     * @return the ObjectFactory for this Module.
     */
    ObjectFactory objectFactory();
}
