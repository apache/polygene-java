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
package org.apache.polygene.api.unitofwork;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.usecase.Usecase;

/**
 * MetaInfo holder for entity-to-value conversion in {@link UnitOfWork#toValue(Class, HasIdentity)}
 * <p>
 *     The implementation of this interface should be registered as metaInfo on the {@link Usecase}
 *     of the {@link UnitOfWork} where the conversion should take place.
 * </p>
 * <code><pre>
 *
 *     private static final Usecase USECASE_GET_USER_DETAILS = UseCaseBuilder
 *                                                                 .buildUseCase("get user details")
 *                                                                 .withMetaInfo( new MyToValueConverter() )
 *                                                                 .newUsecase();
 *
 *     &#64;Structure
 *     private UnitOfWorkFactory uowf;
 *     :
 *     :
 *     try( UnitOfWork uow = uowf.newUnitOfWork( USECASE_GET_USER_DETAILS ) )
 *     {
 *         :
 *         User entity = ...;
 *         User value = uow.toValue( User.class, value );
 *         :
 *     }
 *     :
 *     :
 * </pre></code>
 */
public interface ToValueConverter
{
    /**
     * Returns the Function to convert each of the properties of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @return The function to do the conversion, or null if the default converter should be used.
     */
    Function<PropertyDescriptor, Object> properties( Object entityComposite );

    /**
     * Returns the Function to convert each of the associations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @return The function to do the conversion, or null if the default converter should be used.
     */
    Function<AssociationDescriptor, EntityReference> associations( Object entityComposite );

    /**
     * Returns the Function to convert each of the manyAssociations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @return The function to do the conversion, or null if the default converter should be used.
     */
    Function<AssociationDescriptor, Stream<EntityReference>> manyAssociations( Object entityComposite );

    /**
     * Returns the Function to convert each of the NamedAssociations of the entities into the value.
     *
     * @param entityComposite the entity that is to be converted.
     * @return The function to do the conversion, or null if the default converter should be used.
     */
    Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociations( Object entityComposite );
}
