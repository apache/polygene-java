/*
 * Copyright (c) 2014-2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.conversion.values;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.unitofwork.EntityTypeNotFoundException;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.functional.Iterables;
import org.apache.zest.spi.ZestSPI;

import static org.apache.zest.library.conversion.values.Shared.STRING_COLLECTION_TYPE_SPEC;
import static org.apache.zest.library.conversion.values.Shared.STRING_MAP_TYPE_SPEC;
import static org.apache.zest.library.conversion.values.Shared.STRING_TYPE_SPEC;

/**
 * ValueToEntity Mixin.
 *
 * @deprecated Please use {@link org.apache.zest.api.unitofwork.UnitOfWork#toEntity(Class, Identity)} instead.
 */
public class ValueToEntityMixin
    implements ValueToEntity
{
    private static final QualifiedName IDENTITY_STATE_NAME;
    private static final Function<ManyAssociation<?>, Iterable<EntityReference>> MANY_ASSOC_TO_ENTITY_REF_ITERABLE;
    private static final Function<NamedAssociation<?>, Map<String, EntityReference>> NAMED_ASSOC_TO_ENTITY_REF_MAP;
    private static final Function<Collection<String>, Iterable<EntityReference>> STRING_COLLEC_TO_ENTITY_REF_ITERABLE;
    private static final Function<Map<String, String>, Map<String, EntityReference>> STRING_MAP_TO_ENTITY_REF_MAP;

    static
    {
        try
        {
            IDENTITY_STATE_NAME = QualifiedName.fromAccessor( Identity.class.getMethod( "identity" ) );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Zest Core Runtime codebase is corrupted. Contact Zest team: ValueToEntityMixin" );
        }
        MANY_ASSOC_TO_ENTITY_REF_ITERABLE = manyAssoc -> {
            if( manyAssoc == null )
            {
                return Iterables.empty();
            }
            List<EntityReference> refs = new ArrayList<>( manyAssoc.count() );
            for( Object entity : manyAssoc )
            {
                refs.add( EntityReference.entityReferenceFor( entity ) );
            }
            return refs;
        };
        NAMED_ASSOC_TO_ENTITY_REF_MAP = namedAssoc -> {
            if( namedAssoc == null )
            {
                return Collections.emptyMap();
            }
            Map<String, EntityReference> refs = new LinkedHashMap<>( namedAssoc.count() );
            for( String name : namedAssoc )
            {
                refs.put( name, EntityReference.entityReferenceFor( namedAssoc.get( name ) ) );
            }
            return refs;
        };
        STRING_COLLEC_TO_ENTITY_REF_ITERABLE = stringCollec -> {
            if( stringCollec == null )
            {
                return Iterables.empty();
            }
            return stringCollec.stream()
                .map( EntityReference::parseEntityReference )
                .collect( Collectors.toList() );
        };
        STRING_MAP_TO_ENTITY_REF_MAP = stringMap -> {
            if( stringMap == null )
            {
                return Collections.emptyMap();
            }
            Map<String, EntityReference> refMap = new LinkedHashMap<>( stringMap.size() );
            for( Map.Entry<String, String> entry : stringMap.entrySet() )
            {
                refMap.put( entry.getKey(), EntityReference.parseEntityReference( entry.getValue() ) );
            }
            return refMap;
        };
    }

    @Structure
    private ZestSPI spi;

    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private ModuleDescriptor module;

    @Override
    public <T> T create( Class<T> entityType, Object value )
    {
        return createInstance( doConversion( entityType, null, value ) );
    }

    @Override
    public <T> T create( Class<T> entityType, String identity, Object value )
    {
        return createInstance( doConversion( entityType, identity, value ) );
    }

    @Override
    public <T> T create( Class<T> entityType, Object value, Function<T, T> prototypeOpportunity )
    {
        EntityBuilder<?> builder = doConversion( entityType, null, value );
        //noinspection unchecked
        prototypeOpportunity.apply( (T) builder.instance() );
        return createInstance( builder );
    }

    @Override
    public <T> T create( Class<T> entityType, String identity, Object value, Function<T, T> prototypeOpportunity )
    {
        EntityBuilder<?> builder = doConversion( entityType, identity, value );
        //noinspection unchecked
        prototypeOpportunity.apply( (T) builder.instance() );
        return createInstance( builder );
    }

    @Override
    public <T> Iterable<T> create( final Class<T> entityType, final Iterable<Object> values )
    {
        return Iterables.map(
            value -> create( entityType, value ),
            values
        );
    }

    @Override
    public <T> Iterable<T> create( final Class<T> entityType,
                                   final Iterable<Object> values,
                                   final Function<T, T> prototypeOpportunity
    )
    {
        return Iterables.map(
            value -> create( entityType, value, prototypeOpportunity ),
            values
        );
    }

    private <T> EntityBuilder<?> doConversion( Class<T> entityType, String identity, Object value )
    {
        EntityDescriptor eDesc = module.entityDescriptor( entityType.getName() );
        if( eDesc == null )
        {
            throw EntityTypeNotFoundException.create( entityType.getName(), module );
        }

        ValueComposite vComposite = (ValueComposite) value;

        ValueDescriptor vDesc = spi.valueDescriptorFor( vComposite );
        AssociationStateHolder vState = spi.stateOf( vComposite );
        AssociationStateDescriptor vStateDesc = vDesc.state();

        Unqualified unqualified = vDesc.metaInfo( Unqualified.class );
        if( unqualified == null || !unqualified.value() )
        {
            return doQualifiedConversion( entityType, identity, vState, vStateDesc );
        }
        return doUnqualifiedConversion( entityType, identity, vState, vStateDesc );
    }

    private <T> EntityBuilder<?> doQualifiedConversion(
        Class<T> entityType, String identity,
        final AssociationStateHolder vState, final AssociationStateDescriptor vStateDesc
    )
    {
        Function<PropertyDescriptor, Object> props = ePropDesc -> {
            try
            {
                return vState.propertyFor( ePropDesc.accessor() ).get();
            }
            catch( IllegalArgumentException propNotFoundOnValue )
            {
                // Property not found
                return null;
            }
        };
        Function<AssociationDescriptor, EntityReference> assocs = eAssocDesc -> {
            try
            {
                return EntityReference.entityReferenceFor( vState.associationFor( eAssocDesc.accessor() ) );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Find String Property and convert to Association
                String propName = eAssocDesc.qualifiedName().name();
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( propName );
                    if( STRING_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        String assocState = (String) vState.propertyFor( vPropDesc.accessor() ).get();
                        return EntityReference.parseEntityReference( assocState );
                    }
                    return null;
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return null;
                }
            }
        };
        Function<AssociationDescriptor, Iterable<EntityReference>> manyAssocs = eAssocDesc -> {
            try
            {
                ManyAssociation<Object> vAssocState = vState.manyAssociationFor( eAssocDesc.accessor() );
                return MANY_ASSOC_TO_ENTITY_REF_ITERABLE.apply( vAssocState );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Find Collection<String> Property and convert to ManyAssociation
                String propName = eAssocDesc.qualifiedName().name();
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( propName );
                    if( STRING_COLLECTION_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Collection<String> vAssocState = (Collection) vState.propertyFor( vPropDesc.accessor() ).get();
                        return STRING_COLLEC_TO_ENTITY_REF_ITERABLE.apply( vAssocState );
                    }
                    return Iterables.empty();
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return Iterables.empty();
                }
            }
        };
        Function<AssociationDescriptor, Map<String, EntityReference>> namedAssocs = eAssocDesc -> {
            try
            {
                NamedAssociation<?> vAssocState = vState.namedAssociationFor( eAssocDesc.accessor() );
                return NAMED_ASSOC_TO_ENTITY_REF_MAP.apply( vAssocState );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Find Map<String,String> Property and convert to NamedAssociation
                String propName = eAssocDesc.qualifiedName().name();
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( propName );
                    if( STRING_MAP_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        //noinspection unchecked
                        Map<String, String> vAssocState = (Map) vState.propertyFor( vPropDesc.accessor() ).get();
                        return STRING_MAP_TO_ENTITY_REF_MAP.apply( vAssocState );
                    }
                    return Collections.EMPTY_MAP;
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return Collections.EMPTY_MAP;
                }
            }
        };
        return uowf.currentUnitOfWork().newEntityBuilderWithState(
            entityType, identity, props, assocs, manyAssocs, namedAssocs
        );
    }

    private <T> EntityBuilder<?> doUnqualifiedConversion(
        Class<T> entityType, String identity,
        final AssociationStateHolder vState, final AssociationStateDescriptor vStateDesc
    )
    {
        Function<PropertyDescriptor, Object> props = ePropDesc -> {
            String propName = ePropDesc.qualifiedName().name();
            try
            {
                PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( propName );
                return vState.propertyFor( vPropDesc.accessor() ).get();
            }
            catch( IllegalArgumentException propNotFoundOnValue )
            {
                // Property not found on Value
                return null;
            }
        };
        Function<AssociationDescriptor, EntityReference> assocs = eAssocDesc -> {
            String assocName = eAssocDesc.qualifiedName().name();
            try
            {
                AssociationDescriptor vAssocDesc = vStateDesc.getAssociationByName( assocName );
                Object assocEntity = vState.associationFor( vAssocDesc.accessor() ).get();
                return assocEntity == null ? null : EntityReference.entityReferenceFor( assocEntity );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Association not found on Value, find Property<String> and convert to Association
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( assocName );
                    if( STRING_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        String assocId = (String) vState.propertyFor( vPropDesc.accessor() ).get();
                        return assocId == null ? null : EntityReference.parseEntityReference( assocId );
                    }
                    return null;
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return null;
                }
            }
        };
        Function<AssociationDescriptor, Iterable<EntityReference>> manyAssocs = eAssocDesc -> {
            String assocName = eAssocDesc.qualifiedName().name();
            try
            {
                AssociationDescriptor vAssocDesc = vStateDesc.getManyAssociationByName( assocName );
                ManyAssociation<Object> vManyAssoc = vState.manyAssociationFor( vAssocDesc.accessor() );
                return MANY_ASSOC_TO_ENTITY_REF_ITERABLE.apply( vManyAssoc );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // ManyAssociation not found on Value, find List<String> and convert to ManyAssociation
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( assocName );
                    if( STRING_COLLECTION_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Collection<String> vAssocState = (Collection) vState.propertyFor( vPropDesc.accessor() ).get();
                        return STRING_COLLEC_TO_ENTITY_REF_ITERABLE.apply( vAssocState );
                    }
                    return Iterables.empty();
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return Iterables.empty();
                }
            }
        };
        Function<AssociationDescriptor, Map<String, EntityReference>> namedAssocs = eAssocDesc -> {
            String assocName = eAssocDesc.qualifiedName().name();
            try
            {
                AssociationDescriptor vAssocDesc = vStateDesc.getNamedAssociationByName( assocName );
                NamedAssociation<Object> vAssocState = vState.namedAssociationFor( vAssocDesc.accessor() );
                return NAMED_ASSOC_TO_ENTITY_REF_MAP.apply( vAssocState );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Find Map<String,String> Property and convert to NamedAssociation
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( assocName );
                    if( STRING_MAP_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Map<String, String> vAssocState = (Map) vState.propertyFor( vPropDesc.accessor() ).get();
                        return STRING_MAP_TO_ENTITY_REF_MAP.apply( vAssocState );
                    }
                    return Collections.EMPTY_MAP;
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    return Collections.EMPTY_MAP;
                }
            }
        };
        return uowf.currentUnitOfWork().newEntityBuilderWithState(
            entityType, identity, props, assocs, manyAssocs, namedAssocs
        );
    }

    protected <T> T createInstance( EntityBuilder<?> builder )
    {
        //noinspection unchecked
        return (T) builder.newInstance();
    }

    @Override
    public void update( Object entity, Object value )
        throws NoSuchEntityException
    {
        EntityComposite eComposite = (EntityComposite) entity;
        ValueComposite vComposite = (ValueComposite) value;

        EntityDescriptor eDesc = spi.entityDescriptorFor( eComposite );
        AssociationStateHolder eState = spi.stateOf( eComposite );
        AssociationStateDescriptor eStateDesc = eDesc.state();

        ValueDescriptor vDesc = spi.valueDescriptorFor( vComposite );
        AssociationStateHolder vState = spi.stateOf( vComposite );
        AssociationStateDescriptor vStateDesc = vDesc.state();

        Unqualified unqualified = vDesc.metaInfo( Unqualified.class );
        if( unqualified == null || !unqualified.value() )
        {
            doQualifiedUpdate( eState, eStateDesc, vState, vStateDesc );
        }
        else
        {
            doUnQualifiedUpdate( eState, eStateDesc, vState, vStateDesc );
        }
    }

    private void doQualifiedUpdate(
        AssociationStateHolder eState, AssociationStateDescriptor eStateDesc,
        AssociationStateHolder vState, AssociationStateDescriptor vStateDesc
    )
        throws NoSuchEntityException
    {
        eStateDesc.properties().forEach( ePropDesc ->
        {
            if( ! IDENTITY_STATE_NAME.equals( ePropDesc.qualifiedName() ) )
            {
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByQualifiedName( ePropDesc.qualifiedName() );
                    eState.propertyFor( ePropDesc.accessor() ).set( vState.propertyFor( vPropDesc.accessor() ).get() );
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Property not found on Value, do nothing
                }
            }
        } );
        eStateDesc.associations().forEach( eAssocDesc ->
        {
            Association<Object> eAssoc = eState.associationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssocDesc
                    = vStateDesc.getAssociationByQualifiedName( eAssocDesc.qualifiedName() );
                eAssoc.set( vState.associationFor( vAssocDesc.accessor() ).get() );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Association not found on Value, find Property<String> and load associated Entity
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        String assocId = (String) vState.propertyFor( vPropDesc.accessor() ).get();
                        if( assocId != null )
                        {
                            eAssoc.set( uowf.currentUnitOfWork().get( (Class) eAssocDesc.type(), assocId ) );
                        }
                        else
                        {
                            eAssoc.set( null );
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        });
        eStateDesc.manyAssociations().forEach( eAssocDesc ->
        {
            ManyAssociation<Object> eManyAssoc = eState.manyAssociationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssocDesc
                    = vStateDesc.getManyAssociationByQualifiedName( eAssocDesc.qualifiedName() );
                ManyAssociation<Object> vManyAssoc = vState.manyAssociationFor( vAssocDesc.accessor() );
                eManyAssoc.toList().forEach( eManyAssoc::remove );
                vManyAssoc.toList().forEach( eManyAssoc::add );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // ManyAssociation not found on Value, find Property<List<String>> and load associated Entities
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_COLLECTION_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Collection<String> vAssocState = (Collection) vState.propertyFor( vPropDesc.accessor() ).get();
                        eManyAssoc.toList().forEach( eManyAssoc::remove );
                        if( vAssocState != null )
                        {
                            for( String eachAssoc : vAssocState )
                            {
                                eManyAssoc.add(
                                    uowf.currentUnitOfWork().get( (Class) eAssocDesc.type(), eachAssoc )
                                );
                            }
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        });
        eStateDesc.namedAssociations().forEach( eAssocDesc ->
        {
            NamedAssociation<Object> eNamedAssoc = eState.namedAssociationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssocDesc
                    = vStateDesc.getNamedAssociationByQualifiedName( eAssocDesc.qualifiedName() );
                NamedAssociation<Object> vNamedAssoc = vState.namedAssociationFor( vAssocDesc.accessor() );
                Iterables.toList( eNamedAssoc ).forEach( eNamedAssoc::remove );
                for( Map.Entry<String, Object> assocEntry : vNamedAssoc.toMap().entrySet() )
                {
                    eNamedAssoc.put( assocEntry.getKey(), assocEntry.getValue() );
                }
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // NamedAssociation not found on Value, find Property<Map<String,String>> and load associated Entities
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_MAP_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Map<String, String> vAssocState = (Map) vState.propertyFor( vPropDesc.accessor() ).get();
                        Iterables.toList( eNamedAssoc ).forEach( eNamedAssoc::remove );
                        if( vAssocState != null )
                        {
                            for( Map.Entry<String, String> assocEntry : vAssocState.entrySet() )
                            {
                                eNamedAssoc.put(
                                    assocEntry.getKey(),
                                    uowf.currentUnitOfWork().get( (Class) eAssocDesc.type(), assocEntry.getValue() )
                                );
                            }
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        } );
    }

    private void doUnQualifiedUpdate(
        AssociationStateHolder eState, AssociationStateDescriptor eStateDesc,
        AssociationStateHolder vState, AssociationStateDescriptor vStateDesc
    )
    {
        eStateDesc.properties().forEach( ePropDesc ->
        {
            if( ! IDENTITY_STATE_NAME.equals( ePropDesc.qualifiedName() ) )
            {
                try
                {
                    PropertyDescriptor vPropDesc = vStateDesc.findPropertyModelByName( ePropDesc.qualifiedName().name() );
                    eState.propertyFor( ePropDesc.accessor() ).set( vState.propertyFor( vPropDesc.accessor() ).get() );
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Property not found on Value, do nothing
                }
            }
        } );
        eStateDesc.associations().forEach( eAssocDesc ->
        {
            Association<Object> eAssoc = eState.associationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssocDesc = vStateDesc.getAssociationByName( eAssocDesc.qualifiedName().name() );
                eAssoc.set( vState.associationFor( vAssocDesc.accessor() ).get() );
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // Association not found on Value, find Property<String> and load associated Entity
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        String assocId = (String) vState.propertyFor( vPropDesc.accessor() ).get();
                        if( assocId != null )
                        {
                            eAssoc.set( uowf.currentUnitOfWork().get( (Class) eAssocDesc.type(), assocId ) );
                        }
                        else
                        {
                            eAssoc.set( null );
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        } );
        eStateDesc.manyAssociations().forEach( eAssocDesc ->
        {
            ManyAssociation<Object> eManyAssoc = eState.manyAssociationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssDesc
                    = vStateDesc.getManyAssociationByName( eAssocDesc.qualifiedName().name() );
                ManyAssociation<Object> vManyAss = vState.manyAssociationFor( vAssDesc.accessor() );
                eManyAssoc.toList().forEach( eManyAssoc::remove );
                vManyAss.toList().forEach( eManyAssoc::add );
            }
            catch( IllegalArgumentException assNotFoundOnValue )
            {
                // ManyAssociation not found on Value, find Property<List<String>> and load associated Entities
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_COLLECTION_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Collection<String> vAssocState = (Collection) vState.propertyFor( vPropDesc.accessor() ).get();
                        eManyAssoc.toList().forEach( eManyAssoc::remove );
                        if( vAssocState != null )
                        {
                            vAssocState.forEach( eachAssoc ->
                            {
                                eManyAssoc.add(
                                    uowf.currentUnitOfWork().get( (Class<?>) eAssocDesc.type(), eachAssoc )
                                );
                            } );
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        } );
        eStateDesc.namedAssociations().forEach( eAssocDesc ->
        {
            NamedAssociation<Object> eNamedAssoc = eState.namedAssociationFor( eAssocDesc.accessor() );
            try
            {
                AssociationDescriptor vAssocDesc
                    = vStateDesc.getNamedAssociationByName( eAssocDesc.qualifiedName().name() );
                NamedAssociation<Object> vNamedAssoc = vState.namedAssociationFor( vAssocDesc.accessor() );
                Iterables.toList( eNamedAssoc ).forEach( eNamedAssoc::remove );
                for( Map.Entry<String, Object> assocEntry : vNamedAssoc.toMap().entrySet() )
                {
                    eNamedAssoc.put( assocEntry.getKey(), assocEntry.getValue() );
                }
            }
            catch( IllegalArgumentException assocNotFoundOnValue )
            {
                // NamedAssociation not found on Value, find Property<Map<String,String>> and load associated Entities
                try
                {
                    PropertyDescriptor vPropDesc
                        = vStateDesc.findPropertyModelByName( eAssocDesc.qualifiedName().name() );
                    if( STRING_MAP_TYPE_SPEC.test( vPropDesc.valueType() ) )
                    {
                        @SuppressWarnings( "unchecked" )
                        Map<String, String> vAssocState =
                            (Map<String,String>) vState.propertyFor( vPropDesc.accessor() ).get();
                        Iterables.toList( eNamedAssoc ).forEach( eNamedAssoc::remove );
                        if( vAssocState != null )
                        {
                            for( Map.Entry<String, String> assocEntry : vAssocState.entrySet() )
                            {
                                eNamedAssoc.put(
                                    assocEntry.getKey(),
                                    uowf.currentUnitOfWork().get( (Class) eAssocDesc.type(), assocEntry.getValue() )
                                );
                            }
                        }
                    }
                }
                catch( IllegalArgumentException propNotFoundOnValue )
                {
                    // Do nothing
                }
            }
        } );
    }
}
