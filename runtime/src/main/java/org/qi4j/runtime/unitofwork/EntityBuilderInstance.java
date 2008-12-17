/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.unitofwork;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;

/**
 * TODO
 */
public final class EntityBuilderInstance<T>
    implements EntityBuilder<T>
{
    private static final String NO_IDENTITY = "NO_IDENTITY";

    private static final Method IDENTITY_METHOD;
    private static final Method TYPE_METHOD;
    private static final Method METAINFO_METHOD;
    private static final Method CREATE_METHOD;

    private final ModuleInstance moduleInstance;
    private final EntityModel entityModel;
    private final UnitOfWorkInstance uow;
    private final EntityStore store;
    private final IdentityGenerator identityGenerator;

    private T stateProxy;
    private EntityStateHolder state;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
            TYPE_METHOD = Composite.class.getMethod( "type" );
            METAINFO_METHOD = Composite.class.getMethod( "metaInfo", Class.class );
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: EntityBuilderInstance" );
        }
    }

    public EntityBuilderInstance(
        ModuleInstance moduleInstance, EntityModel entityModel, UnitOfWorkInstance uow, EntityStore store,
        IdentityGenerator identityGenerator )
    {
        this.moduleInstance = moduleInstance;
        this.entityModel = entityModel;
        this.uow = uow;
        this.store = store;
        this.identityGenerator = identityGenerator;
    }

    @SuppressWarnings( "unchecked" )
    public T stateOfComposite()
    {
        // Instantiate proxy for given composite interface
        if( stateProxy == null )
        {
            try
            {
                StateInvocationHandler handler = new StateInvocationHandler();
                ClassLoader proxyClassloader = entityModel.type().getClassLoader();
                Class[] interfaces = new Class[]{ entityModel.type() };
                stateProxy = (T) ( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
            }
            catch( Exception e )
            {
                throw new ConstructionException( e );
            }
        }

        return stateProxy;
    }

    public <K> K stateFor( Class<K> mixinType )
    {
        // Instantiate proxy for given interface
        try
        {
            StateInvocationHandler handler = new StateInvocationHandler();
            ClassLoader proxyClassloader = mixinType.getClassLoader();
            Class[] interfaces = new Class[]{ mixinType };
            return mixinType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public T newInstance()
        throws LifecycleException
    {

        // Figure out whether to use given or generated identity
        boolean prototypePattern = false;
        Property identityProperty = getState().getProperty( IDENTITY_METHOD );
        Object identity = identityProperty.get();
        if( identity == null || identity == NO_IDENTITY )
        {
            Class compositeType = entityModel.type();
            if( identityGenerator == null )
            {
                throw new UnitOfWorkException( "No identity generator found for type " + compositeType.getName() );
            }
            identity = identityGenerator.generate( compositeType );
            identityProperty.set( identity );
            prototypePattern = true;
        }

        // Transfer state
        EntityState entityState = entityModel.newEntityState( store, identity.toString(), state );

        EntityInstance instance = entityModel.loadInstance( uow, store, entityState.qualifiedIdentity(), moduleInstance, entityState );

        Object proxy = instance.proxy();
        uow.createEntity( (EntityComposite) proxy );

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

        if( prototypePattern )
        {
            identityProperty.set( NO_IDENTITY );
        }
        return (T) proxy;
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                T instance = newInstance();
                uow.createEntity( (EntityComposite) instance );
                return instance;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private EntityStateHolder getState()
    {
        if( state == null )
        {
            state = entityModel.newBuilderState();
        }

        return state;
    }

    protected class StateInvocationHandler
        implements InvocationHandler
    {

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return getState().getProperty( method );
            }
            else if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                return getState().getAssociation( method );
            }
            else if( method.equals( TYPE_METHOD ) )
            {
                return entityModel.type();
            }
            else if( method.equals( METAINFO_METHOD ) )
            {
                return entityModel.metaInfo().get( (Class<? extends Object>) objects[ 0 ] );
            }
            else
            {
                throw new IllegalArgumentException( "Method does not represent state: " + method );
            }
        }
    }
}
