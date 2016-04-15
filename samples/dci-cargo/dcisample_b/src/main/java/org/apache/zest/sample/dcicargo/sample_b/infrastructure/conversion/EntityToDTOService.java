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
package org.apache.zest.sample.dcicargo.sample_b.infrastructure.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.value.NoSuchValueException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.functional.Iterables;
import org.apache.zest.library.conversion.values.Unqualified;
import org.apache.zest.spi.ZestSPI;

/**
 * Conversion of Entity objects to DTO's
 *
 * Value composites that extend {@link DTO} will have association properties converted recursively.
 *
 * Modification of {org.apache.zest.library.conversion.values.EntityToValue}
 * WARN No support of NamedAssociations
 */
@SuppressWarnings( "unchecked" )
@Mixins( EntityToDTOService.Mixin.class )
public interface EntityToDTOService
    extends ServiceComposite
{
    <T> T convert( Class<T> valueType, Object entity );

    static abstract class Mixin
        implements EntityToDTOService
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private ZestSPI spi;

        @Structure
        private ModuleDescriptor module;

        @Override
        public <T> T convert( final Class<T> valueType, Object entity )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
            if( valueDescriptor == null )
            {
                throw new NoSuchValueException( valueType.getName(), module.name(), module.typeLookup() );
            }
            Unqualified unqualified = valueDescriptor.metaInfo( Unqualified.class );
            final EntityComposite composite = (EntityComposite) entity;
            final EntityDescriptor entityDescriptor = spi.entityDescriptorFor( composite );
            final AssociationStateHolder associationState = spi.stateOf( composite );
            ValueBuilder<?> builder;

            if( unqualified == null || !unqualified.value() )
            {
                // Copy state using qualified names
                builder = vbf.newValueBuilderWithState( valueType, new Function<PropertyDescriptor, Object>()
                {
                    @Override
                    public Object apply( PropertyDescriptor descriptor )
                    {
                        try
                        {
                            return associationState.propertyFor( descriptor.accessor() ).get();
                        }
                        catch( IllegalArgumentException e )
                        {
                            if( descriptor.valueType().mainType().equals( String.class ) )
                            {
                                // Find Association and convert to string
                                AssociationDescriptor associationDescriptor;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                        .getAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e1 )
                                {
                                    return null;
                                }
                                Object entity = associationState.associationFor( associationDescriptor.accessor() )
                                    .get();
                                if( entity != null )
                                {
                                    return ( (Identity) entity ).identity().get();
                                }
                                else
                                {
                                    return null;
                                }
                            }
                            else if( descriptor.valueType() instanceof CollectionType
                                     && ( (CollectionType) descriptor.valueType() ).collectedType()
                                         .mainType()
                                         .equals( String.class ) )
                            {
                                AssociationDescriptor associationDescriptor;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                        .getManyAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e1 )
                                {
                                    return Collections.emptyList();
                                }

                                ManyAssociation<?> state = associationState.manyAssociationFor( associationDescriptor.accessor() );
                                List<String> entities = new ArrayList<>( state.count() );
                                for( Object entity : state )
                                {
                                    entities.add( ( (Identity) entity ).identity().get() );
                                }
                                return entities;
                            }

                            // No NamedAssociation support

                            return null;
                        }
                    }
                }, new Function<AssociationDescriptor, EntityReference>()
                {
                    @Override
                    public EntityReference apply( AssociationDescriptor associationDescriptor )
                    {
                        return EntityReference.entityReferenceFor(
                            associationState.associationFor( associationDescriptor.accessor() ).get() );
                    }
                }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                {
                    @Override
                    public Iterable<EntityReference> apply( AssociationDescriptor associationDescriptor )
                    {
                        ManyAssociation<?> state = associationState.manyAssociationFor( associationDescriptor.accessor() );
                        List<EntityReference> refs = new ArrayList<>( state.count() );
                        for( Object entity : state )
                        {
                            refs.add( EntityReference.entityReferenceFor( entity ) );
                        }
                        return refs;
                    }
                }, new Function<AssociationDescriptor, Map<String, EntityReference>>()
                {
                    @Override
                    public Map<String, EntityReference> apply( AssociationDescriptor from )
                    {
                        throw new UnsupportedOperationException( "NamedAssociations are not supported." );
                    }
                } );
            }
            else
            {
                builder = vbf.newValueBuilderWithState( valueType, new Function<PropertyDescriptor, Object>()
                {
                    @Override
                    public Object apply( PropertyDescriptor descriptor )
                    {
                        try
                        {
                            PropertyDescriptor propertyDescriptor = entityDescriptor.state()
                                .findPropertyModelByName( descriptor.qualifiedName().name() );
                            return associationState.propertyFor( propertyDescriptor.accessor() ).get();
                        }
                        catch( IllegalArgumentException e )
                        {
                            if( descriptor.valueType().mainType().equals( String.class ) )
                            {
                                // Find Association and convert to string
                                AssociationDescriptor associationDescriptor;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                        .getAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e1 )
                                {
                                    return null;
                                }

                                Object entity = associationState.associationFor( associationDescriptor.accessor() )
                                    .get();
                                if( entity != null )
                                {
                                    return ( (Identity) entity ).identity().get();
                                }
                                return null;
                            }
                            else if( descriptor.valueType() instanceof CollectionType
                                     && ( (CollectionType) descriptor.valueType() ).collectedType()
                                         .mainType()
                                         .equals( String.class ) )
                            {
                                AssociationDescriptor associationDescriptor;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                        .getManyAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e1 )
                                {
                                    return null;
                                }

                                ManyAssociation<?> state = associationState.manyAssociationFor( associationDescriptor.accessor() );
                                List<String> entities = new ArrayList<>( state.count() );
                                for( Object entity : state )
                                {
                                    entities.add( ( (Identity) entity ).identity().get() );
                                }
                                return entities;
                            }

                            // No NamedAssociation support

                            // DTO
                            Class<?> type = descriptor.valueType().mainType();
                            if( DTO.class.isAssignableFrom( type ) )
                            {
                                AssociationDescriptor associationDescriptor;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                        .getAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e1 )
                                {
                                    return null;
                                }

                                Object entity = associationState.associationFor( associationDescriptor.accessor() )
                                    .get();
                                if( entity != null )
                                {
                                    return convert( type, entity );
                                }
                            }

                            return null;
                        }
                    }
                }, new Function<AssociationDescriptor, EntityReference>()
                {
                    @Override
                    public EntityReference apply( AssociationDescriptor descriptor )
                    {
                        AssociationDescriptor associationDescriptor;
                        try
                        {
                            associationDescriptor = entityDescriptor.state()
                                .getAssociationByName( descriptor.qualifiedName().name() );
                        }
                        catch( IllegalArgumentException e )
                        {
                            return null;
                        }

                        return EntityReference.entityReferenceFor( associationState
                                                                       .associationFor( associationDescriptor.accessor() )
                                                                       .get() );
                    }
                }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                {
                    @Override
                    public Iterable<EntityReference> apply( AssociationDescriptor descriptor )
                    {
                        AssociationDescriptor associationDescriptor;
                        try
                        {
                            associationDescriptor = entityDescriptor.state()
                                .getManyAssociationByName( descriptor.qualifiedName().name() );
                        }
                        catch( IllegalArgumentException e )
                        {
                            return Iterables.empty();
                        }

                        ManyAssociation<?> state = associationState.manyAssociationFor( associationDescriptor.accessor() );
                        List<EntityReference> refs = new ArrayList<>( state.count() );
                        for( Object entity : state )
                        {
                            refs.add( EntityReference.entityReferenceFor( entity ) );
                        }
                        return refs;
                    }
                }, new Function<AssociationDescriptor, Map<String, EntityReference>>()
                {
                    @Override
                    public Map<String, EntityReference> apply( AssociationDescriptor from )
                    {
                        throw new UnsupportedOperationException( "NamedAssociations are not supported." );
                    }
                } );
            }

            return (T) builder.newInstance();
        }
    }
}
