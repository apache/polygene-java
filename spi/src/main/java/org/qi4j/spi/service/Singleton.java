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

package org.qi4j.spi.service;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class Singleton
    implements ServiceProvider
{
    private @Structure CompositeBuilderFactory cbf;
    private @Structure ModuleBinding module;

    private Object instance;

    public synchronized Object getService( InjectionResolution injectionResolution, InjectionContext injectionContext )
        throws ServiceProviderException
    {
        if( instance == null )
        {
            Class injectionClass = injectionResolution.getInjectionModel().getInjectionClass();

            CompositeBinding compositeBinding = module.getCompositeBinding( injectionClass );
            if( compositeBinding != null )
            {
                instance = cbf.newCompositeBuilder( compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass() ).newInstance();
            }
            else
            {
                throw new ServiceProviderException( "No Composite type registered which extends the desired service type " + injectionClass.getName() );
            }
        }
        return instance;
    }

    public void releaseService( Object service )
    {
        // Ignore for now
    }
}
