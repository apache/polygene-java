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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.*;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.unitofwork.EntityBuilderInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.QueryBuilderSPI;
import org.qi4j.spi.query.QuerySource;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.qi4j.api.entity.EntityReference.parseEntityReference;

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

    ModuleUnitOfWork( ModuleInstance moduleInstance, UnitOfWorkInstance uow)
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

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance;
    }

    @Override
    public long currentTime()
    {
        return uow.currentTime();
    }

    public Usecase usecase()
    {
        return uow.usecase();
    }

    public MetaInfo metaInfo()
    {
        return uow.metaInfo();
    }

    @Override
    public <T> Query<T> newQuery( QueryBuilder<T> queryBuilder )
    {
        QueryBuilderSPI queryBuilderSPI = (QueryBuilderSPI) queryBuilder;

        return queryBuilderSPI.newQuery( new UoWQuerySource(this) );
    }

    public <T> T newEntity( Class<T> type )
        throws EntityTypeNotFoundException, LifecycleException
    {
        return newEntity( type, null );
    }

    public <T> T newEntity( Class<T> type, String identity )
        throws EntityTypeNotFoundException, LifecycleException
    {
        ModelModule<EntityModel> model = Iterables.first( moduleInstance.findEntityModels( type ));

        if( model == null )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        // Generate id
        if( identity == null )
        {
            identity = model.module().identityGenerator().generate( model.model().type() );
        }

        EntityStore entityStore = model.module().entityStore();

        EntityState entityState = model.model().newEntityState( uow.getEntityStoreUnitOfWork( entityStore, module() ),
                                                              parseEntityReference( identity ) );

        // Init state
        model.model().initState( model.module(), entityState );

        entityState.setProperty( IDENTITY_STATE_NAME, identity );

        EntityInstance instance = new EntityInstance( this, model.module(), model.model(), entityState );

        instance.invokeCreate();

        instance.checkConstraints();

        addEntity( instance );

        return instance.<T>proxy();
    }

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws EntityTypeNotFoundException
    {
        return newEntityBuilder( type, null );
    }

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type, String identity )
        throws EntityTypeNotFoundException
    {
        Iterable<ModelModule<EntityModel>> models = moduleInstance.findEntityModels( type );

        ModelModule<EntityModel> model = Iterables.first( models );

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
                throw new NoSuchCompositeException(IdentityGenerator.class.getName(), model.module().name() );
            }
            identity = idGen.generate( model.model().type() );
        }
        EntityBuilder<T> builder;

        builder = new EntityBuilderInstance<T>( model,
                                                this,
                                                uow.getEntityStoreUnitOfWork( entityStore, moduleInstance ),
                                                identity );
        return builder;
    }

    public <T> T get( Class<T> type, String identity )
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        Iterable<ModelModule<EntityModel>> models = moduleInstance.findEntityModels( type );

        if( !models.iterator().hasNext() )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        return uow.get( parseEntityReference( identity ), this, models, type );
    }

    public <T> T get( T entity )
        throws EntityTypeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        ModelModule<EntityModel> model = new ModelModule<EntityModel>( compositeInstance.module(), compositeInstance.entityModel() );
        Class<T> type = (Class<T>) compositeInstance.type();
        return uow.get( compositeInstance.identity(), this, Collections.singletonList( model), type );
    }

    public void remove( Object entity )
        throws LifecycleException
    {
        uow.checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;

        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );

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

    public void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        uow.complete();
    }

    public void discard()
    {
        uow.discard();
    }

    public boolean isOpen()
    {
        return uow.isOpen();
    }

    public boolean isPaused()
    {
        return uow.isPaused();
    }

    public void pause()
    {
        uow.pause();
    }

    public void resume()
    {
        uow.resume();
    }

    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        uow.addUnitOfWorkCallback( callback );
    }

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

    private class UoWQuerySource implements QuerySource
    {
        private ModuleUnitOfWork moduleUnitOfWork;

        public UoWQuerySource( ModuleUnitOfWork moduleUnitOfWork )
        {
            this.moduleUnitOfWork = moduleUnitOfWork;
        }

        @Override
        public <T> T find( Class<T> resultType, Specification<Composite> whereClause, Iterable<OrderBy> orderBySegments, Integer firstResult, Integer maxResults, Map<String, Object> variables )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module().findService( EntityFinder.class ).get();

            try
            {
                final EntityReference foundEntity = entityFinder.findEntity( resultType, whereClause, variables == null ? Collections.<String, Object>emptyMap() : variables );
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
        public <T> long count( Class<T> resultType, Specification<Composite> whereClause, Iterable<OrderBy> orderBySegments, Integer firstResult, Integer maxResults, Map<String, Object> variables )
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
        public <T> Iterator<T> iterator( final Class<T> resultType, Specification<Composite> whereClause, Iterable<OrderBy> orderBySegments, Integer firstResult, Integer maxResults, Map<String, Object> variables )
        {
            final EntityFinder entityFinder = moduleUnitOfWork.module().findService( EntityFinder.class ).get();

            try
            {
                final Iterator<EntityReference> foundEntities = entityFinder.findEntities( resultType,
                                                                                           whereClause,
                                                                                           Iterables.toArray( OrderBy.class, orderBySegments),
                                                                                           firstResult,
                                                                                           maxResults,
                                                                                           variables == null ? Collections.<String, Object>emptyMap() : variables)
                    .iterator();

                return new Iterator<T>()
                {
                    public boolean hasNext()
                    {
                        return foundEntities.hasNext();
                    }

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
    }
}
