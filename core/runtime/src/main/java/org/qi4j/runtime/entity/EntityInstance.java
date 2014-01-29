/*
 * Copyright (c) 2008-2009, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2009-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
import java.util.Set;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;

/**
 * Entity instance
 */
public final class EntityInstance
    implements CompositeInstance, MixinsInstance
{
    public static EntityInstance entityInstanceOf( EntityComposite composite )
    {
        return (EntityInstance) Proxy.getInvocationHandler( composite );
    }

    private final EntityComposite proxy;
    private final ModuleUnitOfWork uow;
    private final ModuleInstance moduleInstance;
    private final EntityModel entityModel;
    private final EntityReference identity;
    private final EntityState entityState;

    private Object[] mixins;
    private EntityStateInstance state;

    public EntityInstance( ModuleUnitOfWork uow,
                           ModuleInstance moduleInstance,
                           EntityModel entityModel,
                           EntityState entityState
    )
    {
        this.uow = uow;
        this.moduleInstance = moduleInstance;
        this.entityModel = entityModel;
        this.identity = entityState.identity();
        this.entityState = entityState;

        proxy = (EntityComposite) entityModel.newProxy( this );
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        return entityModel.invoke( this, this.proxy, method, args, moduleInstance );
    }

    public EntityReference identity()
    {
        return identity;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T proxy()
    {
        return (T) proxy;
    }

    @Override
    public CompositeDescriptor descriptor()
    {
        return entityModel;
    }

    @Override
    public <T> T newProxy( Class<T> mixinType )
        throws IllegalArgumentException
    {
        return entityModel.newProxy( this, mixinType );
    }

    @Override
    public Object invokeComposite( Method method, Object[] args )
        throws Throwable
    {
        return entityModel.invoke( this, proxy, method, args, moduleInstance );
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return entityModel.metaInfo( infoType );
    }

    public EntityModel entityModel()
    {
        return entityModel;
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return entityModel.types();
    }

    @Override
    public ModuleInstance module()
    {
        return moduleInstance;
    }

    public UnitOfWork unitOfWork()
    {
        return uow;
    }

    public EntityState entityState()
    {
        return entityState;
    }

    @Override
    public EntityStateInstance state()
    {
        if( state == null )
        {
            initState();
        }

        return state;
    }

    public EntityStatus status()
    {
        return entityState.status();
    }

    @Override
    public Object invoke( Object composite, Object[] params, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        if( mixins == null )
        {
            initState();
        }

        Object mixin = methodInstance.getMixinFrom( mixins );

        if( mixin == null )
        {
            mixin = entityModel.newMixin( mixins, state, this, methodInstance.method() );
        }

        return methodInstance.invoke( proxy, params, mixin );
    }

    @Override
    public Object invokeObject( Object proxy, Object[] args, Method method )
        throws Throwable
    {
        return method.invoke( this, args );
    }

    private void initState()
    {
        if( !uow.isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }

        if( status() == EntityStatus.REMOVED )
        {
            throw new NoSuchEntityException( identity, entityModel.types() );
        }

        mixins = entityModel.newMixinHolder();
        state = new EntityStateInstance( entityModel.state(), uow, entityState );
    }

    @Override
    public int hashCode()
    {
        return identity.hashCode();
    }

    @Override
    public boolean equals( Object o )
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

    @Override
    public String toString()
    {
        return identity.toString();
    }

    public void remove( UnitOfWork unitOfWork )
        throws LifecycleException
    {
        invokeRemove();

        removeAggregatedEntities( unitOfWork );

        entityState.remove();
        mixins = null;
    }

    public void invokeCreate()
    {
        lifecyleInvoke( true );
    }

    private void invokeRemove()
    {
        lifecyleInvoke( false );
    }

    private void lifecyleInvoke( boolean create )
    {
        if( mixins == null )
        {
            initState();
        }

        entityModel.invokeLifecycle( create, mixins, this, state );
    }

    private void removeAggregatedEntities( UnitOfWork unitOfWork )
    {
        // Calculate aggregated Entities
        AssociationStateDescriptor stateDescriptor = entityModel.state();
        Set<Object> aggregatedEntities = new HashSet<>();
        Iterable<? extends AssociationDescriptor> associations = stateDescriptor.associations();
        for( AssociationDescriptor association : associations )
        {
            if( association.isAggregated() )
            {
                Association<?> assoc = state.associationFor( association.accessor() );
                Object aggregatedEntity = assoc.get();
                if( aggregatedEntity != null )
                {
                    aggregatedEntities.add( aggregatedEntity );
                }
            }
        }
        Iterable<? extends AssociationDescriptor> manyAssociations = stateDescriptor.manyAssociations();
        for( AssociationDescriptor association : manyAssociations )
        {
            if( association.isAggregated() )
            {
                ManyAssociation<?> manyAssoc = state.manyAssociationFor( association.accessor() );
                for( Object entity : manyAssoc )
                {
                    aggregatedEntities.add( entity );
                }
            }
        }
        Iterable<? extends AssociationDescriptor> namedAssociations = stateDescriptor.namedAssociations();
        for( AssociationDescriptor association : namedAssociations )
        {
            if( association.isAggregated() )
            {
                NamedAssociation<?> namedAssoc = state.namedAssociationFor( association.accessor() );
                for( String name : namedAssoc )
                {
                    aggregatedEntities.add( namedAssoc.get( name ) );
                }
            }
        }

        // Remove aggregated Entities
        for( Object aggregatedEntity : aggregatedEntities )
        {
            unitOfWork.remove( aggregatedEntity );
        }
    }

    public void checkConstraints()
    {
        try
        {
            state.checkConstraints();
        }
        catch( ConstraintViolationException e )
        {
            throw new ConstraintViolationException( identity.identity(), entityModel.types(), e.mixinTypeName(), e.methodName(), e
                .constraintViolations() );
        }
    }
}
