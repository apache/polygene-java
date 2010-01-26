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
import java.util.List;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.unitofwork.EntityBuilderInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.EntityStore;

import static org.qi4j.api.entity.EntityReference.*;

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
            IDENTITY_STATE_NAME = QualifiedName.fromMethod( Identity.class.getMethod( "identity" ) );
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

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return moduleInstance.unitOfWorkFactory();
    }

    public Usecase usecase()
    {
        return uow.usecase();
    }

    public <T> T newEntity( Class<T> type )
        throws EntityTypeNotFoundException, LifecycleException
    {
        return newEntity( type, null );
    }

    public <T> T newEntity( Class<T> type, String identity )
        throws EntityTypeNotFoundException, LifecycleException
    {
        EntityFinder finder = moduleInstance.findEntityModel( type );

        if( finder.noModelExist() )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        if( finder.multipleModelsExists() )
        {
            List<Class<?>> ambiguousTypes = finder.ambigousTypes();
            throw new AmbiguousTypeException( type, ambiguousTypes );
        }

        // Transfer state
        EntityModel entityModel = finder.getFoundModel();
        ModuleInstance entityModuleInstance = finder.getFoundModule();

        // Generate id
        if( identity == null )
        {
            identity = entityModuleInstance.entities().identityGenerator().generate( entityModel.type() );
        }

        EntityStore entityStore = entityModuleInstance.entities().entityStore();

        EntityState entityState = entityModel.newEntityState( uow.getEntityStoreUnitOfWork( entityStore, module() ),
                                                              parseEntityReference( identity ) );

        // Init state
        entityModel.initState( entityState );

        entityState.setProperty( IDENTITY_STATE_NAME, identity );

        EntityInstance instance = new EntityInstance( this, entityModuleInstance, entityModel, entityState );

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
        EntityFinder finder = moduleInstance.findEntityModel( type );

        if( finder.noModelExist() )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        if( finder.multipleModelsExists() )
        {
            List<Class<?>> ambiguousTypes = finder.ambigousTypes();
            throw new AmbiguousTypeException( type, ambiguousTypes );
        }

        EntityModel entityModel = finder.getFoundModel();
        ModuleInstance entityModuleInstance = finder.getFoundModule();
        EntityStore entityStore = entityModuleInstance.entities().entityStore();

        // Generate id if necessary
        if( identity == null )
        {
            IdentityGenerator idGen = entityModuleInstance.entities().identityGenerator();
            identity = idGen.generate( entityModel.type() );
        }

        EntityBuilder<T> builder;

        if( identity != null )
        {
            builder = new EntityBuilderInstance<T>( entityModuleInstance,
                                                    entityModel,
                                                    this,
                                                    uow.getEntityStoreUnitOfWork( entityStore, moduleInstance ),
                                                    identity );
        }
        else
        {
            builder = new EntityBuilderInstance<T>( moduleInstance,
                                                    entityModel,
                                                    this,
                                                    uow.getEntityStoreUnitOfWork( entityModuleInstance.entities().entityStore(), moduleInstance ),
                                                    identity );
        }
        return builder;
    }

    public <T> T get( Class<T> type, String identity )
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        EntityFinder finder = moduleInstance.findEntityModel( type );

        if( finder.noModelExist() )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        return uow.get( parseEntityReference( identity ), this, finder.models(), finder.modules(), type ).<T>proxy();
    }

    public <T> T get( T entity )
        throws EntityTypeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        List<EntityModel> model = Collections.singletonList( compositeInstance.entityModel() );
        List<ModuleInstance> module = Collections.singletonList( compositeInstance.module() );
        Class<? extends EntityComposite> type = compositeInstance.type();
        return uow.get( compositeInstance.identity(), this, model, module, type ).<T>proxy();
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

    public void apply()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        uow.apply();
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
        uow.createEntity( instance );
    }
}
