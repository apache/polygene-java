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
package org.apache.zest.runtime.injection.provider;

import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.util.Classes;
import org.apache.zest.bootstrap.InvalidInjectionException;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.injection.InjectionProvider;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class ModifiesInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( bindingContext.model() instanceof CompositeDescriptor )
        {
            Class<?> type = Classes.RAW_CLASS.apply( dependencyModel.injectionType() );
            if( type.isAssignableFrom( dependencyModel.injectedClass() ) )
            {
                return new ModifiedInjectionProvider();
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + bindingContext.model() + " does not implement @ConcernFor type " + type
                    .getName() + " in modifier " + dependencyModel.injectedClass().getName() );
            }
        }
        else
        {
            throw new InvalidInjectionException( "The class " + dependencyModel.injectedClass()
                .getName() + " is not a modifier" );
        }
    }

    private static class ModifiedInjectionProvider
        implements InjectionProvider
    {
        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return context.next();
        }
    }
}