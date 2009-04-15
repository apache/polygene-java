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

import org.qi4j.api.unitofwork.*;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.*;
import static org.qi4j.api.entity.EntityReference.parseEntityReference;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.StateName;
import org.qi4j.spi.property.PropertyTypeDescriptor;

import java.lang.reflect.Method;

/**
 * JAVADOC
*/
public class ModuleUnitOfWork
    implements UnitOfWork
{
    private static final Method IDENTITY_METHOD;
    private static final Method CREATE_METHOD;
    private static StateName identityStateName;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: ModuleUnitOfWork" );
        }
    }


    private QueryBuilderFactory queryBuilderFactory;

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

    public MetaInfo metaInfo()
    {
        return uow.metaInfo();
    }

    public <T> T newEntity( Class<T> type ) throws EntityTypeNotFoundException, LifecycleException
    {
        return newEntity(type, null);
    }

    public <T> T newEntity(Class<T> type, String identity) throws EntityTypeNotFoundException, LifecycleException
    {
        EntityFinder finder = moduleInstance.findEntityModel(type);

        if( finder.model == null )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        // Register type
        if (!ConfigurationComposite.class.isAssignableFrom(finder.model.type()))
            finder.module.entities().entityTypeRegistry().registerEntityType(finder.model.entityType());

        // Transfer state
        EntityModel entityModel = finder.model;

        // Generate id
        if (identity == null)
        {
            identity = finder.module.entities().identityGenerator().generate( entityModel.type() );
        }

        EntityStore entityStore = finder.module.entities().entityStore();
        EntityState entityState = entityModel.newEntityState( entityStore, parseEntityReference(identity) , moduleInstance.layerInstance().applicationInstance().runtime());

        if (identityStateName == null)
            identityStateName = entityModel.state().<PropertyTypeDescriptor>getPropertyByQualifiedName(QualifiedName.fromMethod(IDENTITY_METHOD)).propertyType().stateName();
        entityState.setProperty(identityStateName, '\"'+identity+'\"');

        EntityInstance instance = new EntityInstance( this, moduleInstance, entityModel, entityState.identity(), entityState );

        instance().createEntity( instance, entityStore );

        // Invoke lifecycle create() method
        if( instance.entityModel().hasMixinType( Lifecycle.class ) )
        {
            try
            {
                instance.invoke( null, CREATE_METHOD, new Object[0] );
            }
            catch( LifecycleException throwable )
            {
                throw throwable;
            }
            catch( Throwable throwable )
            {
                throw new LifecycleException( throwable );
            }
        }

        return instance.<T>proxy();
    }

    public <T> EntityBuilder<T> newEntityBuilder( Class<T> type ) throws EntityTypeNotFoundException
    {
        return newEntityBuilder(type, null);
    }

    public <T> EntityBuilder<T> newEntityBuilder(Class<T> type, String identity)
        throws EntityTypeNotFoundException
    {
        EntityFinder finder = moduleInstance.findEntityModel( type );

        if( finder.model == null )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        // Register type if not a configuration
        if (!ConfigurationComposite.class.isAssignableFrom(finder.model.type()))
            finder.module.entities().entityTypeRegistry().registerEntityType(finder.model.entityType());

        return uow.newEntityBuilder( identity, finder.model, finder.module.entities().entityStore(), finder.module, this );
    }

    public <T> T get(Class<T> type, String identity)
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        EntityFinder finder = moduleInstance.findEntityModel( type );

        if( finder.model == null )
        {
            throw new EntityTypeNotFoundException( type.getName() );
        }

        return uow.get( parseEntityReference(identity), this, finder.model, finder.module ).<T>proxy();
    }

    public <T> T get( T entity ) throws EntityTypeNotFoundException
    {
        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );
        return uow.get( compositeInstance.identity(), this, compositeInstance.entityModel(), compositeInstance.module() ).<T>proxy();
    }

    public void refresh( Object entity ) throws UnitOfWorkException
    {
        uow.refresh( entity );
    }

    public void refresh()
    {
        uow.refresh();
    }

    public void remove( Object entity ) throws LifecycleException
    {
        uow.checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;

        EntityInstance compositeInstance = EntityInstance.getEntityInstance( entityComposite );

        if (compositeInstance.status() == EntityStatus.NEW)
        {
            compositeInstance.remove(this);
            uow.remove(compositeInstance.identity());
        } else if (compositeInstance.status() == EntityStatus.LOADED)
        {
            compositeInstance.remove(this);
        } else
        {
            throw new NoSuchEntityException(compositeInstance.identity());
        }

    }

    public void complete() throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        uow.complete();
    }

    public void apply() throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
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

    public QueryBuilderFactory queryBuilderFactory()
    {
        if( queryBuilderFactory == null )
        {
            ServiceFinder finder = moduleInstance.serviceFinder();
            queryBuilderFactory = new QueryBuilderFactoryImpl( this, moduleInstance.classLoader(), finder );
        }
        return queryBuilderFactory;
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

    @Override public String toString()
    {
        return uow.toString();
    }

}
