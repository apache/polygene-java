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
 */
package org.apache.polygene.entitystore.sql;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.AssociationStateDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.EntityStoreSPI;
import org.apache.polygene.spi.entitystore.EntityStoreUnitOfWork;
import org.apache.polygene.spi.entitystore.StateCommitter;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

import static org.apache.polygene.api.entity.EntityReference.parseEntityReference;

public class SqlEntityStoreMixin
    implements EntityStore, EntityStoreSPI
{
    @This
    private SqlTable sqlTable;

    @Service
    private IdentityGenerator identityGenerator;

    @Service
    private Serialization serialization;

    @Override
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference reference, EntityDescriptor entityDescriptor )
    {
        return new DefaultEntityState( unitOfWork.currentTime(), reference, entityDescriptor );
    }

    @Override
    public EntityState entityStateOf( EntityStoreUnitOfWork unitOfWork, ModuleDescriptor module, EntityReference reference )
    {
        BaseEntity baseEntity = sqlTable.fetchBaseEntity( reference, module );
        SelectQuery<Record> selectQuery = sqlTable.createGetEntityQuery( baseEntity.type, reference );
        Result<Record> result = selectQuery.fetch();
        if( result.isEmpty() )
        {
            throw new EntityNotFoundException( reference );
        }
        return toEntityState( result, baseEntity, reference, module );
    }

    protected EntityState toEntityState( Result<Record> result, BaseEntity baseEntity, EntityReference reference, ModuleDescriptor module )
    {
        AssociationStateDescriptor stateDescriptor = baseEntity.type.state();
        Map<QualifiedName, Object> properties = new HashMap<>();
        properties.put( HasIdentity.IDENTITY_STATE_NAME, baseEntity.identity );
        stateDescriptor.properties()
                       .filter( prop -> !HasIdentity.IDENTITY_STATE_NAME.equals( prop.qualifiedName() ) )
                       .forEach( prop ->
                                 {
                                     QualifiedName qualifiedName = prop.qualifiedName();
                                     Object value = result.getValue( 0, qualifiedName.name() );
                                     value = amendValue( value, prop.valueType(), module );
                                     properties.put( qualifiedName, value );
                                 } );
        Map<QualifiedName, EntityReference> assocations = new HashMap<>();
        stateDescriptor.associations()
                       .forEach( assoc ->
                                 {
                                     QualifiedName qualifiedName = assoc.qualifiedName();
                                     String value = (String) result.getValue( 0, qualifiedName.name() );
                                     if( value != null )
                                     {
                                         assocations.put( qualifiedName, parseEntityReference( value ) );
                                     }
                                 } );
        Map<QualifiedName, List<EntityReference>> manyAssocs = new HashMap<>();
        Map<QualifiedName, Map<String, EntityReference>> namedAssocs = new HashMap<>();
        sqlTable.fetchAssociations( baseEntity, baseEntity.type, associationValue ->
        {
            if( stateDescriptor.hasManyAssociation( associationValue.name ) )
            {
                addManyAssociation( stateDescriptor, manyAssocs, associationValue );
            }
            else if( stateDescriptor.hasNamedAssociation( associationValue.name ) )
            {
                addNamedAssociation( stateDescriptor, namedAssocs, associationValue );
            }
        } );

        return new DefaultEntityState( baseEntity.version,
                                       baseEntity.modifedAt,
                                       reference,
                                       EntityStatus.LOADED,
                                       baseEntity.type,
                                       properties,
                                       assocations,
                                       manyAssocs,
                                       namedAssocs );
    }

    private Object amendValue( Object value, ValueType type, ModuleDescriptor module )
    {
        if( value == null )
        {
            return null;
        }
        if( value.getClass().isPrimitive() )
        {
            return value;
        }
        if( type.equals( ValueType.STRING )
            || type.equals( ValueType.INTEGER )
            || type.equals( ValueType.BOOLEAN )
            || type.equals( ValueType.DOUBLE )
            || type.equals( ValueType.IDENTITY )
            || type.equals( ValueType.LONG )
            || type.equals( ValueType.FLOAT )
            || type.equals( ValueType.BYTE )
            || type.equals( ValueType.CHARACTER )
            || type.equals( ValueType.ENTITY_REFERENCE )
            || type.equals( ValueType.SHORT )
            || type.equals( ValueType.BIG_INTEGER )
            || type.equals( ValueType.BIG_DECIMAL )
            )
        {
            return value;
        }
        if( type.equals( ValueType.INSTANT ) )  // Instant type contains timezone (why?), and we promise to always return in UTC (or is that just bad testcases, and that we actually promise to return original instant timezone?).
        {
            if( value instanceof Instant )
            {
                return Instant.ofEpochMilli( ( (Instant) value ).toEpochMilli() );
            }
            if( value instanceof OffsetDateTime )
            {
                return Instant.ofEpochMilli( ( (OffsetDateTime) value ).toInstant().toEpochMilli() );
            }
            if( value instanceof ZonedDateTime )
            {
                return Instant.ofEpochMilli( ( (ZonedDateTime) value ).toInstant().toEpochMilli() );
            }
        }
        if( type.equals( ValueType.ZONED_DATE_TIME ) )
        {
            if( value instanceof ZonedDateTime )
            {
                return ( (ZonedDateTime) value ).withZoneSameInstant( ZoneOffset.UTC );
            }
            if( value instanceof OffsetDateTime )
            {
                return ( (OffsetDateTime) value ).toZonedDateTime().withZoneSameInstant( ZoneOffset.UTC );
            }
        }
        if( type.equals( ValueType.OFFSET_DATE_TIME ) )
        {
            if( value instanceof OffsetDateTime )
            {
                return ( (OffsetDateTime) value ).withOffsetSameInstant( ZoneOffset.UTC );
            }
            if( value instanceof ZonedDateTime )
            {
                return ( (ZonedDateTime) value ).toOffsetDateTime().withOffsetSameInstant( ZoneOffset.UTC );
            }
        }
        if( type.equals( ValueType.LOCAL_DATE_TIME ) )
        {
            if( value instanceof Timestamp )
            {
                return ( (Timestamp) value ).toLocalDateTime();
            }
        }
        if( type.equals( ValueType.PERIOD ) )
        {
            if( value instanceof String )
            {
                return Period.parse( (String) value );
            }
        }
        if( type.equals( ValueType.DURATION ) )
        {
            if( value instanceof String )
            {
                return Duration.parse( (String) value );
            }
        }
        if( type.equals( ValueType.LOCAL_DATE ) )
        {
            if( value instanceof java.sql.Date )
            {
                return ( (java.sql.Date) value ).toLocalDate();
            }
        }
        if( type.equals( ValueType.LOCAL_TIME ) )
        {
            if( value instanceof java.sql.Time )
            {
                return ( (java.sql.Time) value ).toLocalTime();
            }
        }
        // otherwise, we deal with a serialized value.
        return serialization.deserialize( module, type, (String) value );
    }

    private void addNamedAssociation( AssociationStateDescriptor stateDescriptor, Map<QualifiedName, Map<String, EntityReference>> namedAssocs, AssociationValue associationValue )
    {
        AssociationDescriptor descriptor = stateDescriptor.getNamedAssociationByName( associationValue.name.name() );
        QualifiedName qualifiedName = descriptor.qualifiedName();
        Map<String, EntityReference> map = namedAssocs.computeIfAbsent( qualifiedName, k -> new HashMap<>() );
        map.put( associationValue.position, parseEntityReference( associationValue.reference ) );
    }

    private void addManyAssociation( AssociationStateDescriptor stateDescriptor, Map<QualifiedName, List<EntityReference>> manyAssocs, AssociationValue associationValue )
    {
        AssociationDescriptor descriptor = stateDescriptor.getManyAssociationByName( associationValue.name.name() );
        QualifiedName qualifiedName = descriptor.qualifiedName();
        List<EntityReference> list = manyAssocs.computeIfAbsent( qualifiedName, k -> new ArrayList<>() );
        String reference = associationValue.reference;
        list.add( reference == null ? null : parseEntityReference( reference ) );
    }

    @Override
    public String versionOf( EntityStoreUnitOfWork unitOfWork, EntityReference reference )
    {
        BaseEntity baseEntity = sqlTable.fetchBaseEntity( reference, unitOfWork.module() );
        return baseEntity.version;
    }

    @Override
    public StateCommitter applyChanges( EntityStoreUnitOfWork unitOfWork, Iterable<EntityState> state )
    {
        return new JooqStateCommitter( unitOfWork, state, sqlTable.jooqDslContext() );
    }

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module,
                                                 this,
                                                 identityGenerator.generate( SqlEntityStoreService.class ),
                                                 usecase,
                                                 currentTime
        );
    }

    @Override
    public Stream<EntityState> entityStates( ModuleDescriptor module )
    {
        Stream<? extends EntityDescriptor> entityTypes = module.entityComposites();
        return entityTypes
            .flatMap( type -> sqlTable.fetchAll( type, module ) )
            .map( baseEntity ->
                  {
                      EntityReference reference = EntityReference.entityReferenceFor( baseEntity.identity );
                      SelectQuery<Record> selectQuery = sqlTable.createGetEntityQuery( baseEntity.type, reference );
                      Result<Record> result = selectQuery.fetch();
                      return toEntityState( result, baseEntity, reference, module );
                  } );
    }

    private class JooqStateCommitter
        implements StateCommitter
    {
        private final EntityStoreUnitOfWork unitOfWork;
        private final Iterable<EntityState> states;
        private final JooqDslContext dslContext;
        private final ModuleDescriptor module;

        JooqStateCommitter( EntityStoreUnitOfWork unitOfWork, Iterable<EntityState> states, JooqDslContext dslContext )
        {
            this.unitOfWork = unitOfWork;
            this.states = states;
            this.dslContext = dslContext;
            this.module = unitOfWork.module();
        }

        private void newState( DefaultEntityState state, EntityStoreUnitOfWork unitOfWork )
        {
            EntityReference ref = state.entityReference();
            EntityDescriptor descriptor = state.entityDescriptor();
            sqlTable.createNewBaseEntity( ref, descriptor, this.unitOfWork );
            sqlTable.insertEntity( state, sqlTable.fetchBaseEntity( ref, module ), unitOfWork );
        }

        private void updateState( DefaultEntityState state, EntityStoreUnitOfWork unitOfWork )
        {
            EntityDescriptor descriptor = state.entityDescriptor();
            BaseEntity baseEntity = sqlTable.fetchBaseEntity( state.entityReference(), descriptor.module() );
            sqlTable.updateEntity( state, baseEntity, unitOfWork );
        }

        private void removeState( DefaultEntityState state )
        {
            EntityReference reference = state.entityReference();
            EntityDescriptor descriptor = state.entityDescriptor();
            sqlTable.removeEntity( reference, descriptor );
        }

        @Override
        public void commit()
        {
            dslContext.transaction( configuration ->
                                    {
                                        for( EntityState es : this.states )
                                        {
                                            DefaultEntityState state = (DefaultEntityState) es;
                                            if( state.status() == EntityStatus.NEW )
                                            {
                                                newState( state, unitOfWork );
                                            }
                                            if( state.status() == EntityStatus.UPDATED )
                                            {
                                                updateState( state, unitOfWork );
                                            }
                                            if( state.status() == EntityStatus.REMOVED )
                                            {
                                                removeState( state );
                                            }
                                        }
                                    } );
        }

        @Override
        public void cancel()
        {
        }
    }
}
