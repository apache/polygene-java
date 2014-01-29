/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.library.conversion.values.Unqualified;
import org.qi4j.spi.Qi4jSPI;

/**
 * Conversion of Entity objects to DTO's
 *
 * Value composites that extend {@link DTO} will have association properties converted recursively.
 *
 * Modification of {org.qi4j.library.conversion.values.EntityToValue}
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
        private Qi4jSPI spi;

        @Structure
        private Module module;

        @Override
        public <T> T convert( final Class<T> valueType, Object entity )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
            if( valueDescriptor == null )
            {
                throw new NoSuchValueException( valueType.getName(), module.name() );
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
                    public Object map( PropertyDescriptor descriptor )
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
                                Object entity = associationState.associationFor( associationDescriptor.accessor() ).get();
                                if( entity != null )
                                {
                                    return ( (Identity) entity ).identity().get();
                                }
                                return null;
                            }
                            else if( descriptor.valueType() instanceof CollectionType
                                     && ( (CollectionType) descriptor.valueType() ).collectedType().mainType().equals( String.class ) )
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
                    public EntityReference map( AssociationDescriptor associationDescriptor )
                    {
                        return EntityReference.entityReferenceFor(
                            associationState.associationFor( associationDescriptor.accessor() ).get() );
                    }
                }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                {
                    @Override
                    public Iterable<EntityReference> map( AssociationDescriptor associationDescriptor )
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
                    public Map<String, EntityReference> map( AssociationDescriptor from )
                    {
                        // No NamedAssociation support
                        return Collections.emptyMap();
                    }
                } );
            }
            else
            {
                builder = vbf.newValueBuilderWithState( valueType, new Function<PropertyDescriptor, Object>()
                {
                    @Override
                    public Object map( PropertyDescriptor descriptor )
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

                                Object entity = associationState.associationFor( associationDescriptor.accessor() ).get();
                                if( entity != null )
                                {
                                    return ( (Identity) entity ).identity().get();
                                }
                                return null;
                            }
                            else if( descriptor.valueType() instanceof CollectionType
                                     && ( (CollectionType) descriptor.valueType() ).collectedType().mainType().equals( String.class ) )
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

                                Object entity = associationState.associationFor( associationDescriptor.accessor() ).get();
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
                    public EntityReference map( AssociationDescriptor descriptor )
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
                            .associationFor( associationDescriptor.accessor() ).get() );
                    }
                }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                {
                    @Override
                    public Iterable<EntityReference> map( AssociationDescriptor descriptor )
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
                    public Map<String, EntityReference> map( AssociationDescriptor from )
                    {
                        // No NamedAssociations support
                        return Collections.emptyMap();
                    }
                } );
            }

            return (T) builder.newInstance();
        }
    }

}
