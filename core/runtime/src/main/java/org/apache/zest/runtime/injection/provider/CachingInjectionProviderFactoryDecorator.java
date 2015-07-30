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

package org.apache.zest.runtime.injection.provider;

import org.apache.zest.bootstrap.InvalidInjectionException;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionProvider;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.model.Resolution;

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
