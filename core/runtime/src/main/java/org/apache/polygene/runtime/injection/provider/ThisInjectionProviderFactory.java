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
package org.apache.polygene.runtime.injection.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.bootstrap.InvalidInjectionException;
import org.apache.polygene.runtime.composite.ProxyGenerator;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.injection.InjectionProvider;
import org.apache.polygene.runtime.injection.InjectionProviderFactory;
import org.apache.polygene.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class ThisInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    @SuppressWarnings( "unchecked" )
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( bindingContext.model() instanceof CompositeDescriptor )
        {
            // If Composite type then return real type, otherwise use the specified one
            final Class<?> thisType = dependencyModel.rawInjectionType();

            Stream<Class<?>> injectionTypes;
            if( Classes.assignableTypeSpecification( thisType ).test( bindingContext.model() ) )
            {
                injectionTypes = bindingContext.model().types();
            }
            else
            {
                CompositeDescriptor acd = ( (CompositeDescriptor) bindingContext.model() );
                injectionTypes = acd.mixinTypes().filter( thisType::isAssignableFrom );
            }

            List<Class<?>> classes = injectionTypes.collect( Collectors.toList() );
            if( classes.size() == 0 )
            {
                throw new InvalidInjectionException( "Composite " + bindingContext.model()
                                                     + " does not implement @This type " + thisType.getName() + " in fragment "
                                                     + dependencyModel.injectedClass().getName() );
            }
            return new ThisInjectionProvider( classes );
        }
        else
        {
            throw new InvalidInjectionException( "Object " + dependencyModel.injectedClass() + " may not use @This" );
        }
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    private static class ThisInjectionProvider
        implements InjectionProvider
    {
        Constructor proxyConstructor;
        private Class[] interfaces;

        private ThisInjectionProvider( List<Class<?>> types )
        {
            try
            {
                Class proxyClass;
                Class<?> mainType = types.get( 0 );
                if( Proxy.class.isAssignableFrom( mainType ) )
                {
                    proxyClass = mainType;
                }
                else
                {
                    interfaces = types.stream().map( Class.class::cast ).toArray( Class[]::new );
                    proxyClass = ProxyGenerator.createProxyClass( mainType.getClassLoader(), interfaces );
                }

                proxyConstructor = proxyClass.getConstructor( InvocationHandler.class );
            }
            catch( Exception e )
            {
                // Ignore
                e.printStackTrace();
            }
        }

        @Override
        public Object provideInjection( InjectionContext context )
        {
            try
            {
                InvocationHandler handler = context.compositeInstance();
                if( handler == null )
                {
                    handler = context.proxyHandler();
                }
                return proxyConstructor.newInstance( handler );
            }
            catch( Exception e )
            {
                throw new InjectionProviderException( "Could not instantiate @This proxy", e );
            }
        }
    }
}
