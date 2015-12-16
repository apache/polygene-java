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

package org.apache.zest.spi.structure;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.value.ValueDescriptor;

public interface TypeLookup
{
    /**
     * Lookup first Entity Model matching the given Type.
     *
     * <p>First, if Entity Models exactly match the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Second, if Entity Models match a type assignable to the given type, the closest one (Visibility then Assembly order) is returned.
     * Multiple <b>assignable</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for creational use cases only.</b> For non-creational use cases see
     * {@link #lookupEntityModels(Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return First matching Entity Model
     */
    ModelModule<EntityDescriptor> lookupEntityModel( Class type );

    /**
     * Lookup all Entity Models matching the given Type.
     *
     * <p>Returned Iterable contains, in order, Entity Models that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>forbidden</b> and result in an AmbiguousTypeException.</p>
     * <p>Multiple <b>assignable</b> matches are <b>allowed</b> to enable polymorphic fetches and queries.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p><b>Should be used for non-creational use cases only.</b> For creational use cases see
     * {@link #lookupEntityModel(Class)}.</p>
     *
     * @param type Looked up Type
     *
     * @return All matching Entity Models
     */
    Iterable<ModelModule<EntityDescriptor>> lookupEntityModels( Class type );

    /**
     * Lookup first ServiceReference matching the given Type.
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * <p>See {@link #lookupServiceReferences(Type)}.</p>
     *
     * @param <T>         Service Type
     * @param serviceType Looked up Type
     *
     * @return First matching ServiceReference
     */
    <T> ServiceReference<T> lookupServiceReference( Type serviceType );

    /**
     * Lookup all ServiceReferences matching the given Type.
     *
     * <p>Returned Iterable contains, in order, ServiceReferences that: </p>
     *
     * <ul>
     * <li>exactly match the given type, in Visibility then Assembly order ;</li>
     * <li>match a type assignable to the given type, in Visibility then Assembly order.</li>
     * </ul>
     *
     * <p>Multiple <b>exact</b> matches with the same Visibility are <b>allowed</b> to enable polymorphic lookup/injection.</p>
     * <p>Multiple <b>assignable</b> matches with the same Visibility are <b>allowed</b> for the very same reason.</p>
     *
     * <p>Type lookup is done lazily and cached.</p>
     *
     * @param <T>  Service Type
     * @param type Looked up Type
     *
     * @return All matching ServiceReferences
     */
    <T> List<ServiceReference<T>> lookupServiceReferences( Type type );

    Stream<Class<?>> allVisibleObjects();

    Stream<ModelModule<ObjectDescriptor>> allObjects();

    Stream<ModelModule<TransientDescriptor>> allTransients();

    Stream<ModelModule<ValueDescriptor>> allValues();

    Stream<ModelModule<EntityDescriptor>> allEntities();

    Stream<ServiceReference<?>> allServices();
}
