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

package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.ModuleInstance;

import static org.qi4j.api.util.Classes.RAW_CLASS;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.functional.Iterables.*;

/**
 * JAVADOC
 */
public abstract class AbstractModifierModel
    implements Dependencies, VisitableHierarchy<Object, Object>
{
    private final Class<?> modifierClass;

    private ConstructorsModel constructorsModel;
    private InjectedFieldsModel injectedFieldsModel;
    private InjectedMethodsModel injectedMethodsModel;

    private Class<?>[] nextInterfaces;

    public AbstractModifierModel( Class<?> declaredModifierClass, Class<?> instantiationClass )
    {
        this.modifierClass = instantiationClass;
        constructorsModel = new ConstructorsModel( modifierClass );
        injectedFieldsModel = new InjectedFieldsModel( declaredModifierClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredModifierClass );
        Class<Class<?>> componentType = (Class<Class<?>>) Class.class.cast( Class.class );
        nextInterfaces = toArray( componentType, unique( map( RAW_CLASS, interfacesOf( declaredModifierClass ) ) ));
    }

    public Class<?> modifierClass()
    {
        return modifierClass;
    }

    @Override
    public Iterable<DependencyModel> dependencies()
    {
        return flattenIterables( map( DEPENDENCIES_FUNCTION, iterable( constructorsModel, injectedFieldsModel, injectedMethodsModel ) ) );
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
    public InvocationHandler newInstance( ModuleInstance moduleInstance,
                                          InvocationHandler next,
                                          ProxyReferenceInvocationHandler proxyHandler,
                                          Method method
    )
    {
        InjectionContext injectionContext = new InjectionContext( moduleInstance, wrapNext( next ), proxyHandler );

        Object modifier = constructorsModel.newInstance( injectionContext );

        try
        {
            if( FragmentClassLoader.isGenerated( modifier ) )
            {
                modifier.getClass().getField( "_instance" ).set( modifier, proxyHandler );
            }
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace();
        }
        catch( NoSuchFieldException e )
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

        if( !modifierClass.equals( that.modifierClass ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return modifierClass.hashCode();
    }
}