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

package org.qi4j.runtime.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConstructionException;
import org.qi4j.composite.State;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.entity.Lifecycle;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.property.ImmutablePropertyInstance;

/**
 * TODO
 */
public final class EntityBuilderInstance<T>
    implements EntityBuilder<T>
{
    private static final Method IDENTITY_METHOD;
    private static final Method TYPE_METHOD;
    private static final Method METAINFO_METHOD;

    private final ModuleInstance moduleInstance;
    private final EntityModel entityModel;
    private final UnitOfWorkInstance uow;
    private final EntityStore store;
    private final IdentityGenerator identityGenerator;

    private T stateProxy;
    private State state;

    static
    {
        try
        {
            IDENTITY_METHOD = Identity.class.getMethod( "identity" );
            TYPE_METHOD = Composite.class.getMethod( "type" );
            METAINFO_METHOD = Composite.class.getMethod( "metaInfo", Class.class );
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
    {

        // Figure out whether to use given or generated identity
        boolean prototypePattern = false;
        Property identityProperty = getState().getProperty( IDENTITY_METHOD );
        Object identity = identityProperty.get();
        if( identity == null )
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

        EntityInstance instance = entityModel.loadInstance( uow, store, entityState.getIdentity(), moduleInstance, entityState );

        Object proxy = instance.proxy();
        uow.createEntity( (EntityComposite) proxy );

        // Invoke lifecycle create() method
        if( Lifecycle.class.isAssignableFrom( instance.type() ) )
        {
//            context.invokeCreate( instance, compositeInstance );
        }

        if( prototypePattern )
        {
            identityProperty.set( ImmutablePropertyInstance.UNSET );
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

    private State getState()
    {
        if( state == null )
        {
            state = entityModel.newDefaultState();
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
