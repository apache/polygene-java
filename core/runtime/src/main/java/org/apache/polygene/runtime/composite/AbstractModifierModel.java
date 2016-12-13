/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectedFieldsModel;
import org.apache.polygene.runtime.injection.InjectedMethodsModel;
import org.apache.polygene.runtime.injection.InjectionContext;

import static org.apache.polygene.api.util.Classes.RAW_CLASS;
import static org.apache.polygene.api.util.Classes.interfacesOf;

/**
 * JAVADOC
 */
public abstract class AbstractModifierModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    private final Class<?> modifierClass;

    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;

    private final Class<Class<?>>[] nextInterfaces;

    @SuppressWarnings( "unchecked" )
    public AbstractModifierModel( Class<?> declaredModifierClass, Class<?> instantiationClass )
    {
        this.modifierClass = instantiationClass;
        constructorsModel = new ConstructorsModel( modifierClass );
        injectedFieldsModel = new InjectedFieldsModel( declaredModifierClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredModifierClass );
        Class<Class<?>> componentType = (Class<Class<?>>) Class.class.cast( Class.class );
        nextInterfaces = interfacesOf( declaredModifierClass )
            .map( RAW_CLASS )
            .distinct()
            .toArray( size -> (Class<Class<?>>[]) Array.newInstance( componentType, size ) );
    }

    public Class<?> modifierClass()
    {
        return modifierClass;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Stream<DependencyModel> dependencies()
    {
        Stream<? extends Dependencies> models = Stream.of( this.constructorsModel, injectedFieldsModel, injectedMethodsModel );
        return models.flatMap( Dependencies::dependencies );
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( modifierClass );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( constructorsModel.accept( visitor ) )
            {
                if( injectedFieldsModel.accept( visitor ) )
                {
                    injectedMethodsModel.accept( visitor );
                }
            }
        }

        return visitor.visitLeave( this );
    }

    // Context
    public InvocationHandler newInstance( ModuleDescriptor module,
                                          InvocationHandler next,
                                          ProxyReferenceInvocationHandler proxyHandler,
                                          Method method
    )
    {
        InjectionContext injectionContext = new InjectionContext( module, wrapNext( next ), proxyHandler );

        Object modifier = constructorsModel.newInstance( injectionContext );

        try
        {
            if( FragmentClassLoader.isGenerated( modifier ) )
            {
                modifier.getClass().getField( "_instance" ).set( modifier, proxyHandler );
            }
        }
        catch( IllegalAccessException | NoSuchFieldException e )
        {
            e.printStackTrace();
        }

        injectedFieldsModel.inject( injectionContext, modifier );
        injectedMethodsModel.inject( injectionContext, modifier );

        if( isGeneric() )
        {
            return (InvocationHandler) modifier;
        }
        else
        {
            try
            {
                Method invocationMethod = modifierClass.getMethod( "_" + method.getName(), method.getParameterTypes() );
                TypedModifierInvocationHandler handler = new TypedModifierInvocationHandler();
                handler.setFragment( modifier );
                handler.setMethod( invocationMethod );
                return handler;
            }
            catch( NoSuchMethodException e )
            {
                throw new ConstructionException( "Could not find modifier method", e );
            }
        }
    }

    private Object wrapNext( InvocationHandler next )
    {
        if( isGeneric() )
        {
            return next;
        }
        else
        {
            return Proxy.newProxyInstance( modifierClass.getClassLoader(), nextInterfaces, next );
        }
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
        AbstractModifierModel that = (AbstractModifierModel) o;
        return modifierClass.equals( that.modifierClass );
    }

    @Override
    public int hashCode()
    {
        return modifierClass.hashCode();
    }
}