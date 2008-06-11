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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.State;
import org.qi4j.property.Property;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public class CompositeBuilderInstance<T>
    implements CompositeBuilder<T>
{
    protected ModuleInstance moduleInstance;
    protected CompositeModel compositeModel;
    private UsesInstance uses;
    private Class<T> compositeType;
    private T stateProxy;
    private State state;

    public CompositeBuilderInstance( ModuleInstance moduleInstance, CompositeModel compositeModel )
    {
        this.moduleInstance = moduleInstance;

        this.compositeModel = compositeModel;
        compositeType = (Class<T>) compositeModel.type();
    }

    public Class<T> compositeType()
    {
        return compositeType;
    }

    public CompositeBuilder<T> use( Object... usedObjects )
    {
        getUses().use( usedObjects );

        return this;
    }

    public T stateOfComposite()
    {
        // Instantiate proxy for given composite interface
        if( stateProxy == null )
        {
            try
            {
                StateInvocationHandler handler = new StateInvocationHandler();
                ClassLoader proxyClassloader = compositeType.getClassLoader();
                Class[] interfaces = new Class[]{ compositeType };
                stateProxy = compositeType.cast( Proxy.newProxyInstance( proxyClassloader, interfaces, handler ) );
            }
            catch( Exception e )
            {
                throw new InstantiationException( e );
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
            throw new InstantiationException( e );
        }
    }

    public T newInstance() throws org.qi4j.composite.InstantiationException
    {
        CompositeInstance compositeInstance = compositeModel.newCompositeInstance( moduleInstance, uses == null ? UsesInstance.NO_USES : uses, state );
        state = null; // Reset state - TODO should create a copy lazily
        return compositeType.cast( compositeInstance.proxy() );
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
                return newInstance();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected UsesInstance getUses()
    {
        if( uses == null )
        {
            uses = new UsesInstance();
        }
        return uses;
    }

    private State getState()
    {
        if( state == null )
        {
            state = compositeModel.newDefaultState();
        }

        return state;
    }

    protected class StateInvocationHandler
        implements InvocationHandler
    {
        public StateInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return getState().getProperty( method );
            }
            else
            {
                throw new IllegalArgumentException( "Method does not represent state: " + method );
            }
        }
    }
}
