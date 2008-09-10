/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.Identity;
import org.qi4j.entity.Lifecycle;
import org.qi4j.entity.LifecycleException;
import org.qi4j.entity.LoadingPolicy;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public final class EntityInstance
    implements CompositeInstance, MixinsInstance
{
    private static final Method REMOVE_METHOD;


    static
    {
        try
        {
            REMOVE_METHOD = Lifecycle.class.getMethod( "remove" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: EntityInstance" );
        }
    }


    public static EntityInstance getEntityInstance( Composite composite )
    {
        return (EntityInstance) Proxy.getInvocationHandler( composite );
    }

    private final EntityComposite proxy;
    private final UnitOfWorkInstance uow;
    private final EntityStore store;
    private final EntityModel entity;
    private final ModuleInstance moduleInstance;
    private final QualifiedIdentity identity;

    private Object[] mixins;
    private EntityState entityState;
    private EntityStatus status;
    private EntityStateModel.EntityStateInstance state;

    public EntityInstance( UnitOfWorkInstance uow, EntityStore store, EntityModel entity, ModuleInstance moduleInstance, QualifiedIdentity identity, EntityStatus status, EntityState entityState )
    {
        this.uow = uow;
        this.store = store;
        this.entity = entity;
        this.moduleInstance = moduleInstance;
        this.identity = identity;
        this.status = status;

        // If we have a recording LoadingPolicy, wrap the EntityState
        LoadingPolicy loadingPolicy = uow.loadingPolicy();
        if( loadingPolicy != null && loadingPolicy.isRecording() )
        {
            entityState = new RecordingEntityState( entityState, loadingPolicy );
        }

        this.entityState = entityState;

        proxy = entity.newProxy( this );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return entity.invoke( this, this.proxy, method, args, moduleInstance );
    }

    public QualifiedIdentity qualifiedIdentity()
    {
        return identity;
    }

    public EntityComposite proxy()
    {
        return proxy;
    }

    public MetaInfo metaInfo()
    {
        return entity.metaInfo();
    }

    public Class<? extends EntityComposite> type()
    {
        return entity.type();
    }

    public Object[] mixins()
    {
        return mixins;
    }

    public void setMixins( Object[] mixins )
    {
        this.mixins = mixins;
    }

    public ModuleInstance module()
    {
        return moduleInstance;
    }


    public EntityStore store()
    {
        return store;
    }

    public EntityState entityState()
    {
        return entityState;
    }

    public EntityStateModel.EntityStateInstance state()
    {
        return state;
    }

    public void setEntityState( EntityStateModel.EntityStateInstance state )
    {
        this.state = state;
    }

    public EntityStatus status()
    {
        return entityState != null ? entityState.status() : status;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance ) throws Throwable
    {
        if( mixins == null )
        {
            if( status() == EntityStatus.REMOVED )
            {
                throw new EntityCompositeNotFoundException( identity.identity(), entity.type() );
            }
            if( entityState == null )
            {
                entityState = entity.getEntityState( store, identity );
            }
            mixins = entity.newMixins( uow, entityState, this );
        }

        return entity.invoke( composite, params, mixins, methodInstance );
    }

    public Object invokeObject( Object proxy, Object[] args, Method method ) throws Throwable
    {
        return method.invoke( this, args );
    }

    public void refresh()
    {
        if( status() == EntityStatus.LOADED && entityState != null )
        {
            // Only refresh if the state has actually changed
            EntityState newEntityState = store.getEntityState( identity );
            if( newEntityState.version() != entityState.version() )
            {
                refresh( newEntityState );
            }
        }
    }

    private void refresh( EntityState newState )
    {
        entityState = newState;

        state.refresh( newState );
    }


    @Override public int hashCode()
    {
        return identity.hashCode();
    }

    @Override public boolean equals( Object o )
    {
        try
        {
            Identity other = ( (Identity) o );
            return other != null && identity.identity().equals( other.identity().get() );
        }
        catch( ClassCastException e )
        {
            return false;
        }
    }

    @Override public String toString()
    {
        return identity.toString();
    }

    public void cast( EntityInstance newEntityInstance )
    {
        Object[] newMixins = newEntityInstance.mixins;

        // Use any mixins that match the ones we already have
        for( int i = 0; i < mixins.length; i++ )
        {
            Object oldMixin = mixins[ i ];
            for( Object newMixin : newMixins )
            {
                if( oldMixin.getClass().equals( newMixin.getClass() ) )
                {
                    newMixins[ i ] = oldMixin;
                    break;
                }
            }
        }

    }

    public boolean isReference()
    {
        return mixins == null;
    }

    public EntityState load()
    {
        if( entityState == null && status() == EntityStatus.LOADED )
        {
            entityState = entity.getEntityState( store, identity );
        }

        return entityState;
    }

    public void remove()
        throws LifecycleException
    {
        if( entity.hasMixinType( Lifecycle.class ) )
        {
            try
            {
                invoke( proxy, REMOVE_METHOD, new Object[0] );
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

        status = EntityStatus.REMOVED;
        entityState = null;
        mixins = null;
    }

    public EntityModel entityModel()
    {
        return entity;
    }
}
