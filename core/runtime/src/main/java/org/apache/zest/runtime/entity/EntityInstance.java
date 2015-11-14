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
package org.apache.zest.runtime.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateDescriptor;
import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.entity.LifecycleException;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkException;
import org.apache.zest.runtime.composite.CompositeMethodInstance;
import org.apache.zest.runtime.composite.MixinsInstance;
import org.apache.zest.runtime.structure.ModuleUnitOfWork;
import org.apache.zest.spi.entity.EntityState;
import org.apache.zest.spi.entity.EntityStatus;
import org.apache.zest.spi.module.ModuleSpi;

import static java.util.stream.Collectors.toList;

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
    private final ModuleSpi moduleInstance;
    private final EntityModel entityModel;
    private final EntityReference identity;
    private final EntityState entityState;

    private Object[] mixins;
    private EntityStateInstance state;

    public EntityInstance( ModuleUnitOfWork uow,
                           ModuleSpi moduleInstance,
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
    public Stream<Class<?>> types()
    {
        return entityModel.types();
    }

    @Override
    public ModuleSpi module()
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
            throw new NoSuchEntityException( identity, entityModel.types(), unitOfWork().usecase() );
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
        if( Boolean.getBoolean( "zest.entity.print.state" ) )
        {
            return state.toString();
        }
        else
        {
            return identity.toString();
        }
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
        Stream.concat(
            stateDescriptor.associations()
                .filter( AssociationDescriptor::isAggregated )
                .map( association -> state.associationFor( association.accessor() ).get() )
                .filter( Objects::nonNull ),

            Stream.concat(
                stateDescriptor.manyAssociations()
                    .filter( AssociationDescriptor::isAggregated )
                    .flatMap( association -> state.manyAssociationFor( association.accessor() ).toList().stream() )
                    .filter( Objects::nonNull ),

                stateDescriptor.namedAssociations()
                    .filter( AssociationDescriptor::isAggregated )
                    .flatMap( association -> state.namedAssociationFor( association.accessor() )
                        .toMap()
                        .values()
                        .stream() )
                    .filter( Objects::nonNull )
            )
        ).distinct().collect( Collectors.toList() ).stream().forEach( unitOfWork::remove );
    }

    public void checkConstraints()
    {
        try
        {
            state.checkConstraints();
        }
        catch( ConstraintViolationException e )
        {
            List<Class<?>> entityModelList = entityModel.types().collect( toList() );
            throw new ConstraintViolationException( identity.identity(),
                                                    entityModelList,
                                                    e.mixinTypeName(),
                                                    e.methodName(),
                                                    e.constraintViolations() );
        }
    }
}
