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

package org.apache.polygene.library.restlet.repository;

import java.util.function.Predicate;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.unitofwork.NoSuchEntityException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.library.restlet.identity.IdentityManager;

public class SmallCrudRepositoryMixin<T extends HasIdentity>
    implements CrudRepository<T>
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private QueryBuilderFactory qbf;

    @Service
    private IdentityManager identityManager;

    private final Class<T> entityType;

    @SuppressWarnings( "unchecked" )
    public SmallCrudRepositoryMixin( @Structure PolygeneAPI api, @This ServiceComposite me )
    {
        entityType = api.serviceDescriptorFor( me ).metaInfo( EntityTypeDescriptor.class ).entityType();
    }

    @Override
    public void create( Identity identity )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        uow.newEntity( entityType, identity );
    }

    @Override
    public T get( Identity identity )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        return uow.get( entityType, identity );
    }

    @Override
    public void update( T newStateAsValue )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        @SuppressWarnings( "unchecked" )
        Class<HasIdentity> type = (Class<HasIdentity>) entityType;

        uow.toEntity( type, newStateAsValue );  //updates the identified entity with the value
    }

    @Override
    public void delete( Identity identity )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        try
        {
            T entity = uow.get( entityType, identity );
            uow.remove( entity );
        }
        catch( NoSuchEntityException | NoSuchEntityTypeException e )
        {
            throw new IllegalArgumentException( "Entity  '" + identity + "' doesn't exist." );
        }
    }

    @Override
    public Iterable<T> findAll()
    {
        return find( item -> true );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Iterable<T> find( Predicate<Composite> specification )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilder<T> qb = qbf.newQueryBuilder( entityType );
        Query<T> query = uow.newQuery( qb );
        return qbf.newQueryBuilder( entityType ).where( specification ).newQuery( query );
    }

    @Override
    public T toValue( T entity )
    {
        return uowf.currentUnitOfWork().toValue( entityType, entity );
    }
}
