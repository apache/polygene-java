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
import org.apache.polygene.api.composite.NoSuchTransientException;
import org.apache.polygene.api.object.NoSuchObjectException;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.InvalidInjectionException;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.injection.InjectionProvider;
import org.apache.polygene.runtime.injection.InjectionProviderFactory;
import org.apache.polygene.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private static class UsesInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = uses.useForType( injectionType );

            if( usesObject == null && !dependency.optional() )
            {
                // No @Uses object provided
                // Try instantiating a Transient or Object for the given type
                Module moduleInstance = context.module().instance();

                try
                {
                    if( context.instance() != null )
                    {
                        uses = uses.use( context.instance() );
                    }
                    usesObject = moduleInstance.newTransient( injectionType, uses.toArray() );
                }
                catch( NoSuchTransientException e )
                {
                    try
                    {
                        usesObject = moduleInstance.newObject( injectionType, uses.toArray() );
                    }
                    catch( NoSuchObjectException e1 )
                    {
                        // Could not instantiate an instance - to try instantiate as plain class
                        try
                        {
                            usesObject = injectionType.newInstance();
                        }
                        catch( Throwable e2 )
                        {
                            // Could not instantiate - try with this as first argument
                            try
                            {
                                Constructor constructor = injectionType.getDeclaredConstructor( context.instance()
                                                                                                    .getClass() );
                                if( !constructor.isAccessible() )
                                {
                                    constructor.setAccessible( true );
                                }
                                usesObject = constructor.newInstance( context.instance() );
                            }
                            catch( Throwable e3 )
                            {
                                // Really can't instantiate it - ignore
                            }
                        }
                    }
                }

                if( usesObject != null )
                {
                    context.setUses( context.uses().use( usesObject ) ); // Use this for other injections in same graph
                }

                return usesObject;
            }
            else
            {
                return usesObject;
            }
        }
    }
}