/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.spi.injection;

/**
 * If a dependency resolution should be a singleton, wrap it with this
 * to provide a single instance "cache".
 */
public class SingletonInjectionDecorator
    implements InjectionProvider
{
    private InjectionProvider decoratedProvider;
    private Object singletonInstance;

    public SingletonInjectionDecorator( InjectionProvider injectionProvider )
    {
        this.decoratedProvider = injectionProvider;
    }

    public synchronized Object provideInjection( InjectionContext context )
    {
        if( singletonInstance == null )
        {
            singletonInstance = decoratedProvider.provideInjection( context );
        }

        return singletonInstance;
    }
}
