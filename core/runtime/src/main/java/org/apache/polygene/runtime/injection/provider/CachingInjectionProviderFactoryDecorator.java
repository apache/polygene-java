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

import org.apache.polygene.bootstrap.InvalidInjectionException;
import org.apache.polygene.runtime.injection.DependencyModel;
import org.apache.polygene.runtime.injection.InjectionProvider;
import org.apache.polygene.runtime.injection.InjectionProviderFactory;
import org.apache.polygene.runtime.model.Resolution;

/**
 * JAVADOC
 */
public class CachingInjectionProviderFactoryDecorator
    implements InjectionProviderFactory
{
    private final InjectionProviderFactory decoratedFactory;

    public CachingInjectionProviderFactoryDecorator( InjectionProviderFactory decoratedFactory )
    {
        this.decoratedFactory = decoratedFactory;
    }

    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        InjectionProvider injectionProvider = decoratedFactory.newInjectionProvider( resolution, dependencyModel );
        if( injectionProvider != null )
        {
            return new CachingInjectionProviderDecorator( injectionProvider );
        }
        else
        {
            return null;
        }
    }
}
