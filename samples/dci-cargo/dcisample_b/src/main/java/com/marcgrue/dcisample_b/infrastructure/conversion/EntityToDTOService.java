package com.marcgrue.dcisample_b.infrastructure.conversion;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Conversion of Entity objects to DTO's
 *
 * Value composites that extend {@link DTO} will have association properties converted recursively.
 *
 * Modification of {org.qi4j.library.conversion.values.EntityToValue}
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

        public <T> T convert( final Class<T> valueType, Object entity )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );
            if( valueDescriptor == null )
            {
                throw new NoSuchValueException( valueType.getName(), module.name() );
            }
            Unqualified unqualified = valueDescriptor.metaInfo( Unqualified.class );
            Iterable<? extends PropertyDescriptor> properties = valueDescriptor.state().properties();
            final EntityComposite composite = (EntityComposite) entity;
            final EntityDescriptor entityDescriptor = spi.getEntityDescriptor( composite );
            final AssociationStateHolder associationState = spi.getState( composite );
            ValueBuilder builder;

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
                                    return associationState.propertyFor( descriptor.accessor() )
                                            .get();
                                }
                                catch( IllegalArgumentException e )
                                {
                                    if( descriptor.valueType()
                                            .mainType()
                                            .equals( String.class ) )
                                    {
                                        // Find Association and convert to string
                                        AssociationDescriptor associationDescriptor = null;
                                        try
                                        {
                                            associationDescriptor = entityDescriptor.state()
                                                    .getAssociationByName( descriptor.qualifiedName()
                                                            .name() );
                                        }
                                        catch( IllegalArgumentException e1 )
                                        {
                                            return null;
                                        }
                                        Object entity = associationState.associationFor( associationDescriptor
                                                .accessor() )
                                                .get();
                                        if( entity != null )
                                        {
                                            return ( (Identity) entity ).identity()
                                                    .get();
                                        }
                                        else
                                        {
                                            return null;
                                        }
                                    }
                                    else if( descriptor.valueType() instanceof CollectionType && ( (CollectionType) descriptor
                                            .valueType() ).collectedType()
                                            .mainType()
                                            .equals( String.class ) )
                                    {
                                        AssociationDescriptor associationDescriptor = null;
                                        try
                                        {
                                            associationDescriptor = entityDescriptor.state()
                                                    .getManyAssociationByName( descriptor.qualifiedName()
                                                            .name() );
                                        }
                                        catch( IllegalArgumentException e1 )
                                        {
                                            return Collections.emptyList();
                                        }

                                        ManyAssociation state = associationState.manyAssociationFor( associationDescriptor
                                                .accessor() );
                                        List<String> entities = new ArrayList<String>();
                                        for( Object entity : state )
                                        {
                                            entities.add( ( (Identity) entity ).identity()
                                                    .get() );
                                        }
                                        return entities;
                                    }

                                    return null;
                                }
                            }
                        }, new Function<AssociationDescriptor, EntityReference>()
                        {
                            @Override
                            public EntityReference map( AssociationDescriptor associationDescriptor )
                            {
                                return EntityReference.getEntityReference( associationState
                                        .associationFor( associationDescriptor
                                                .accessor() )
                                        .get() );
                            }
                        }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                        {
                            @Override
                            public Iterable<EntityReference> map( AssociationDescriptor associationDescriptor )
                            {
                                List<EntityReference> refs = new ArrayList<EntityReference>();
                                for( Object entity : associationState.manyAssociationFor( associationDescriptor.accessor() ) )
                                {
                                    refs.add( EntityReference.getEntityReference( entity ) );
                                }
                                return refs;
                            }
                        }
                );
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
                                    PropertyDescriptor propertyDescriptor = entityDescriptor
                                            .state()
                                            .getPropertyByName( descriptor.qualifiedName()
                                                    .name() );
                                    return associationState.propertyFor( propertyDescriptor
                                            .accessor() )
                                            .get();
                                }
                                catch( Exception e )
                                {
                                    if( descriptor.valueType()
                                            .mainType()
                                            .equals( String.class ) )
                                    {
                                        // Find Association and convert to string
                                        AssociationDescriptor associationDescriptor = null;
                                        try
                                        {
                                            associationDescriptor = entityDescriptor.state()
                                                    .getAssociationByName( descriptor.qualifiedName()
                                                            .name() );
                                        }
                                        catch( IllegalArgumentException e1 )
                                        {
                                            return null;
                                        }

                                        Object entity = associationState.associationFor( associationDescriptor
                                                .accessor() )
                                                .get();
                                        if( entity != null )
                                        {
                                            return ( (Identity) entity ).identity()
                                                    .get();
                                        }
                                        else
                                        {
                                            return null;
                                        }
                                    }
                                    else if( descriptor.valueType() instanceof CollectionType && ( (CollectionType) descriptor
                                            .valueType() ).collectedType()
                                            .mainType()
                                            .equals( String.class ) )
                                    {
                                        AssociationDescriptor associationDescriptor = null;
                                        try
                                        {
                                            associationDescriptor = entityDescriptor.state()
                                                    .getManyAssociationByName( descriptor.qualifiedName()
                                                            .name() );
                                        }
                                        catch( IllegalArgumentException e1 )
                                        {
                                            return null;
                                        }

                                        ManyAssociation state = associationState.manyAssociationFor( associationDescriptor
                                                .accessor() );
                                        List<String> entities = new ArrayList<String>();
                                        for( Object entity : state )
                                        {
                                            entities.add( ( (Identity) entity ).identity()
                                                    .get() );
                                        }
                                        return entities;
                                    }

                                    // DTO
                                    Class<?> type = descriptor.valueType().mainType();
                                    if ( DTO.class.isAssignableFrom(type)) {
                                        AssociationDescriptor associationDescriptor = null;
                                        try
                                        {
                                            associationDescriptor = entityDescriptor.state()
                                                    .getAssociationByName( descriptor.qualifiedName()
                                                            .name() );
                                        }
                                        catch( IllegalArgumentException e1 )
                                        {
                                            return null;
                                        }

                                        Object entity = associationState.associationFor( associationDescriptor
                                                .accessor() )
                                                .get();
                                        if( entity != null )
                                        {
                                            return convert( type, entity);
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
                                AssociationDescriptor associationDescriptor = null;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                            .getAssociationByName( descriptor.qualifiedName()
                                                    .name() );
                                }
                                catch( Exception e )
                                {
                                    return null;
                                }

                                return EntityReference.getEntityReference( associationState
                                        .associationFor( associationDescriptor
                                                .accessor() )
                                        .get() );
                            }
                        }, new Function<AssociationDescriptor, Iterable<EntityReference>>()
                        {
                            @Override
                            public Iterable<EntityReference> map( AssociationDescriptor descriptor )
                            {
                                AssociationDescriptor associationDescriptor = null;
                                try
                                {
                                    associationDescriptor = entityDescriptor.state()
                                            .getManyAssociationByName( descriptor.qualifiedName().name() );
                                }
                                catch( IllegalArgumentException e )
                                {
                                    return Iterables.empty();
                                }

                                List<EntityReference> refs = new ArrayList<EntityReference>();
                                for( Object entity : associationState.manyAssociationFor( associationDescriptor.accessor() ) )
                                {
                                    refs.add( EntityReference.getEntityReference( entity ) );
                                }
                                return refs;
                            }
                        }
                );
            }

            return (T) builder.newInstance();
        }
    }
}
