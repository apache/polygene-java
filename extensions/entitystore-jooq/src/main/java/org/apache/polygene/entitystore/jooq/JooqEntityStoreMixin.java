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
package org.apache.polygene.entitystore.jooq;

import java.time.Instant;
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
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.spi.PolygeneSPI;
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

public class JooqEntityStoreMixin
    implements EntityStore, EntityStoreSPI
{

    @Structure
    private PolygeneSPI spi;

    @This
    private SqlType sqlType;

    @This
    private SqlTable sqlTable;

    @This
    private JooqDslContext jooqDslContext;

    @Service
    private IdentityGenerator identityGenerator;

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
        AssociationStateDescriptor stateDescriptor = baseEntity.type.state();
        Map<QualifiedName, Object> properties = new HashMap<>();
        stateDescriptor.properties().forEach( prop ->
                                              {
                                                  QualifiedName qualifiedName = prop.qualifiedName();
                                                  Object value = result.getValue( 0, qualifiedName.name() );
                                                  properties.put( qualifiedName, value );
                                              } );
        Map<QualifiedName, EntityReference> assocations = new HashMap<>();
        stateDescriptor.associations().forEach( assoc ->
                                                {
                                                    QualifiedName qualifiedName = assoc.qualifiedName();
                                                    String value = (String) result.getValue( 0, qualifiedName.name() );
                                                    assocations.put( qualifiedName, parseEntityReference( value ) );
                                                } );
        Map<QualifiedName, List<EntityReference>> manyAssocs = new HashMap<>();
        Map<QualifiedName, Map<String, EntityReference>> namedAssocs = new HashMap<>();
        result.forEach( record ->
                        {
                            sqlTable.fetchAssociations( record, associationValue ->
                            {
                                // TODO: Perhaps introduce "preserveManyAssociationOrder" option which would have an additional column, separating 'ordinal position' and 'name position'
                                if( associationValue.position == null )
                                {
                                    addManyAssociation( stateDescriptor, manyAssocs, associationValue );
                                }
                                else
                                {
                                    addNamedAssociation( stateDescriptor, namedAssocs, associationValue );
                                }
                            } );
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

    private void addNamedAssociation( AssociationStateDescriptor stateDescriptor, Map<QualifiedName, Map<String, EntityReference>> namedAssocs, AssociationValue associationValue )
    {
        AssociationDescriptor descriptor = stateDescriptor.getNamedAssociationByName( associationValue.name );
        QualifiedName qualifiedName = descriptor.qualifiedName();
        Map<String, EntityReference> map = namedAssocs.computeIfAbsent( qualifiedName, k -> new HashMap<>() );
        map.put( associationValue.position, parseEntityReference( associationValue.reference ) );
    }

    private void addManyAssociation( AssociationStateDescriptor stateDescriptor, Map<QualifiedName, List<EntityReference>> manyAssocs, AssociationValue associationValue )
    {
        AssociationDescriptor descriptor = stateDescriptor.getManyAssociationByName( associationValue.name );
        QualifiedName qualifiedName = descriptor.qualifiedName();
        List<EntityReference> list = manyAssocs.computeIfAbsent( qualifiedName, k -> new ArrayList<>() );
        list.add( parseEntityReference( associationValue.reference ) );
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
        return new JooqStateCommitter( unitOfWork, state );
    }

    @Override
    public EntityStoreUnitOfWork newUnitOfWork( ModuleDescriptor module, Usecase usecase, Instant currentTime )
    {
        return new DefaultEntityStoreUnitOfWork( module,
                                                 this,
                                                 identityGenerator.generate( JooqEntityStoreService.class ),
                                                 usecase,
                                                 currentTime
        );
    }

    @Override
    public Stream<EntityState> entityStates( ModuleDescriptor module )
    {
        return null;
    }

    private static class JooqStateCommitter
        implements StateCommitter
    {
        public JooqStateCommitter( EntityStoreUnitOfWork unitOfWork, Iterable<EntityState> state )
        {

        }

        @Override
        public void commit()
        {

        }

        @Override
        public void cancel()
        {

        }
    }
}
