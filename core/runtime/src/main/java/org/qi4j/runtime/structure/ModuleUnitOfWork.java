/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.structure;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.NoSuchServiceException;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.unitofwork.EntityBuilderInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.QueryBuilderSPI;
import org.qi4j.spi.query.QuerySource;

import static org.qi4j.api.entity.EntityReference.parseEntityReference;
import static org.qi4j.functional.Iterables.first;

/**
 * JAVADOC
 */
public class ModuleUnitOfWork
    implements UnitOfWork
{
    private static final QualifiedName IDENTITY_STATE_NAME;

    static
    {
        try
        {
            IDENTITY_STATE_NAME = QualifiedName.fromAccessor( Identity.class.getMethod( "identity" ) );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: ModuleUnitOfWork" );
        }
    }

    private UnitOfWorkInstance uow;
    private ModuleInstance moduleInstance;

    ModuleUnitOfWork( ModuleInstance moduleInstance, UnitOfWorkInstance uow )
    {
        this.moduleInstance = moduleInstance;
        this.uow = uow;
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public UnitOfWorkInstance instance()
    {
        return uow;
    }

    @Override
    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance;
    }

    @Override
    public long currentTime()
    {
        return uow.currentTime();
    }

    @Override
    public Usecase usecase()
    {
        return uow.usecase();
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return uow.metaInfo().get( infoType );
    }

    @Override
    public void setMetaInfo( Object metaInfo )
    {
        uow.metaInfo().set( metaInfo );
    }

    @Override
    public <T> Query<T> newQuery( QueryBuilder<T> queryBuilder )
    {
        QueryBuilderSPI queryBuilderSPI = (QueryBuilderSPI) queryBuilder;

        return queryBuilderSPI.newQuery( new UoWQuerySource( this ) );
    }

    @Override
    public <T> T newEntity( Class<T> type )
        throws EntityTypeNotFoundException, LifecycleException
    {
        return newEntity( type, null );
    }

    @Override
    public <T> T newEntity( Class<T> type, String identity )
        throws EntityTypeNotFoundException, LifecycleException
    {
        return newEntityBuilder( type, identity ).newInstance();
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws EntityTypeNotFoundException
    {
        return newEntityBuilder( type, null );
    }

    @Override
    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type, String identity )
        throws EntityTypeNotFoundException
    {
        ModelModule<EntityModel> model = moduleInstance.typeLookup().lookupEntityModel( type );

        if( model == null )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        EntityStore entityStore = model.module().entityStore();

        // Generate id if necessary
        if( identity == null )
        {
            IdentityGenerator idGen = model.module().identityGenerator();
            if( idGen == null )
            {
                throw new NoSuchServiceException( IdentityGenerator.class.getName(), model.module().name() );
            }
            identity = idGen.generate( first( model.model().types() ) );
        }
        EntityBuilder<T> builder;

        builder = new EntityBuilderInstance<T>( model,
                                                this,
                                                uow.getEntityStoreUnitOfWork( entityStore, moduleInstance ),
                                                identity );
        return builder;
    }

    @Override
    public <T> T get( Class<T> type, String identity )
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        Iterable<ModelModule<EntityModel>> models = moduleInstance.typeLookup().lookupEntityModels( type );

        if( !models.iterator().hasNext() )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        return uow.get( parseEntityReference( identity ), this, models, type );
    }

    @Override
    public <T> T get( T entity )
        throws EntityTypeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.entityInstanceOf( entityComposite );
        ModelModule<EntityModel> model = new ModelModule<EntityModel>( compositeInstance.module(), compositeInstance.entityModel() );
        Class<T> type = (Class<T>) first( compositeInstance.types() );
        return uow.get( compositeInstance.identity(), this, Collections.singletonList( model ), type );
    }

    @Override
    public void remove( Object entity )
        throws LifecycleException
    {
        uow.checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;

        EntityInstance compositeInstance = EntityInstance.entityInstanceOf( entityComposite );

        if( compositeInstance.status() == EntityStatus.NEW )
        {
            compositeInstance.remove( this );
            uow.remove( compositeInstance.identity() );
        }
        else if( compositeInstance.status() == EntityStatus.LOADED || compositeInstance.status() == EntityStatus.UPDATED )
        {
            compositeInstance.remove( this );
        }
        else
        {
            throw new NoSuchEntityException( compositeInstance.identity() );
        }
    }

    @Override
    public void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        uow.complete();
    }

    @Override
    public void discard()
    {
        uow.discard();
    }

    @Override
    public boolean isOpen()
    {
        return uow.isOpen();
    }

    @Override
    public boolean isPaused()
    {
        return uow.isPaused();
    }

    @Override
    public void pause()
    {
        uow.pause();
    }

    @Override
    public void resume()
    {
        uow.resume();
    }

    @Override
    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        uow.addUnitOfWorkCallback( callback );
    }

    @Override
    public void removeUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        uow.removeUnitOfWorkCallback( callback );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ModuleUnitOfWork that = (ModuleUnitOfWork) o;

        if( !uow.equals( that.uow ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return uow.hashCode();
    }

    @Override
    public String toString()
    {
        return uow.toString();
    }

    public void addEntity( EntityInstance instance )
    {
        uow.addEntity( instance );
    }

    private static class UoWQuerySource implements QuerySource
    {
        private ModuleUnitOfWork moduleUnitOfWork;

        public UoWQuerySource( ModuleUnitOfWork moduleUnitOfWork )
        {
            this.moduleUnitOfWork = moduleUnitOfWork;
        }

        @Override
        public <T> T find( Class<T> resultType,
                           Specification<Composite> whereClause,
                           Iterable<OrderBy> orderBySegments,
                           Integer firstResult,
                           Integer maxResults,
                           Map<String, Object> variables
        )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module().findService( EntityFinder.class ).get();

            try
            {
                final EntityReference foundEntity = entityFinder.findEntity( resultType, whereClause, variables == null ? Collections
                    .<String, Object>emptyMap() : variables );
                if( foundEntity != null )
                {
                    try
                    {
                        return moduleUnitOfWork.get( resultType, foundEntity.identity() );
                    }
                    catch( NoSuchEntityException e )
                    {
                        return null; // Index is out of sync - entity has been removed
                    }
                }
                // No entity was found
                return null;
            }
            catch( EntityFinderException e )
            {
                throw new QueryExecutionException( "Finder caused exception", e );
            }
        }

        @Override
        public <T> long count( Class<T> resultType,
                               Specification<Composite> whereClause,
                               Iterable<OrderBy> orderBySegments,
                               Integer firstResult,
                               Integer maxResults,
                               Map<String, Object> variables
        )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module().findService( EntityFinder.class ).get();

            try
            {
                return entityFinder.countEntities( resultType, whereClause, variables == null ? Collections.<String, Object>emptyMap() : variables );
            }
            catch( EntityFinderException e )
            {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public <T> Iterator<T> iterator( final Class<T> resultType,
                                         Specification<Composite> whereClause,
                                         Iterable<OrderBy> orderBySegments,
                                         Integer firstResult,
                                         Integer maxResults,
                                         Map<String, Object> variables
        )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module().findService( EntityFinder.class ).get();

            try
            {
                final Iterator<EntityReference> foundEntities = entityFinder.findEntities( resultType,
                                                                                           whereClause,
                                                                                           Iterables.toArray( OrderBy.class, orderBySegments ),
                                                                                           firstResult,
                                                                                           maxResults,
                                                                                           variables == null ? Collections
                                                                                               .<String, Object>emptyMap() : variables )
                    .iterator();

                return new Iterator<T>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return foundEntities.hasNext();
                    }

                    @Override
                    public T next()
                    {
                        final EntityReference foundEntity = foundEntities.next();
                        try
                        {
                            return moduleUnitOfWork.get( resultType, foundEntity.identity() );
                        }
                        catch( NoSuchEntityException e )
                        {
                            // Index is out of sync - entity has been removed
                            return null;
                        }
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            catch( EntityFinderException e )
            {
                throw new QueryExecutionException( "Query '" + toString() + "' could not be executed", e );
            }
        }

        @Override
        public String toString()
        {
            return "UnitOfWork( " + moduleUnitOfWork.usecase().name() + " )";
        }
    }
}
