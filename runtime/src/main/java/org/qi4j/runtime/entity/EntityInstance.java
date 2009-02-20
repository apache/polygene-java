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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.unitofwork.EntityCompositeNotFoundException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.StateUsage;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.unitofwork.RecordingEntityState;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateDescriptor;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.association.AssociationDescriptor;

/**
 * JAVADOC
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

    public EntityInstance( UnitOfWorkInstance uow, EntityStore store, EntityModel entity, ModuleInstance moduleInstance, QualifiedIdentity identity, EntityStatus status, EntityState entityState)
    {
        this.uow = uow;
        this.store = store;
        this.entity = entity;
        this.moduleInstance = moduleInstance;
        this.identity = identity;
        this.status = status;

        this.entityState = wrapEntityState( entityState);

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

    public UnitOfWork unitOfWork()
    {
        return uow;
    }

    public EntityState entityState()
    {
        EntityState unwrappedState = entityState;
        while (unwrappedState instanceof EntityStateAdapter)
            unwrappedState = (( EntityStateAdapter) unwrappedState).wrappedEntityState();

        return unwrappedState;
    }

    public EntityStateModel.EntityStateInstance state()
    {
        if( state == null )
        {
            initState();
        }

        return state;
    }

    public void setEntityState( EntityStateModel.EntityStateInstance state )
    {
        this.state = state;
    }

    public EntityStatus status()
    {
        return entityState() != null ? entityState.status() : status;
    }

    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance ) throws Throwable
    {
        if( mixins == null )
        {
            initState();
        }

        Object mixin = methodInstance.getMixin( mixins );

        if (mixin == null)
        {
            mixin = entity.newMixin( mixins, state, this, methodInstance.method() );
        }

        return methodInstance.invoke( composite, params, mixin );
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

    private void initState()
    {
        if( status() == EntityStatus.REMOVED )
        {
            throw new EntityCompositeNotFoundException( entity.type().getName() );
        }
        if( entityState() == null )
        {
            entityState = entity.getEntityState( store, identity );
        }
        mixins = entity.initialize( uow, entityState, this );
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
            return other != null && other.identity().get().equals( identity.identity() );
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
            entityState = wrapEntityState( entity.getEntityState( store, identity ) );
        }

        return entityState;
    }

    private EntityState wrapEntityState( EntityState entityState )
    {
        // If we have a recording LoadingPolicy, wrap the EntityState
        StateUsage stateUsage = uow.usecase().stateUsage();
        if( stateUsage != null && stateUsage.isRecording() )
        {
            entityState = new RecordingEntityState( entityState, stateUsage );
        }

        return entityState;
    }

    public void remove()
        throws LifecycleException
    {
        invokeRemove();

        removeAggregatedEntities();


        status = EntityStatus.REMOVED;
        entityState = null;
        mixins = null;
    }

    private void invokeRemove()
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
    }

    private void removeAggregatedEntities()
    {
        // Calculate aggregated Entities
        EntityStateDescriptor stateDescriptor = entity.state();
        List<AssociationDescriptor> associations = stateDescriptor.associations();
        Set<Object> aggregatedEntities = new HashSet<Object>();
        for( AssociationDescriptor association : associations )
        {
            if (association.isAggregated())
            {
                AbstractAssociation assoc = state.getAssociation( association.accessor() );
                if (assoc instanceof Association )
                {
                    Object aggregatedEntity = ((Association)assoc).get();
                    if (aggregatedEntity != null)
                    {
                        aggregatedEntities.add(aggregatedEntity);
                    }
                } else
                {
                    ManyAssociation manyAssoc = (ManyAssociation) assoc;
                    aggregatedEntities.addAll( manyAssoc );
                }
            }
        }

        // Remove aggregated Entities
        for( Object aggregatedEntity : aggregatedEntities )
        {
            unitOfWork().remove( aggregatedEntity );
        }
    }

    public EntityModel entityModel()
    {
        return entity;
    }
}
