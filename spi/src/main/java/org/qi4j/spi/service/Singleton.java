/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Structure;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.service.ServiceComposite;

/**
 * TODO
 */
public final class Singleton
    implements ServiceProvider
{
    private @Structure CompositeBuilderFactory cbf;
    private @Structure ModuleBinding module;

    private ServiceComposite instance;

    public synchronized <T extends ServiceComposite> T  getService( InjectionResolution injectionResolution,
                                                                    InjectionContext injectionContext )
        throws ServiceProviderException
    {
        if( instance == null )
        {
            Class injectionClass = injectionResolution.getInjectionModel().getInjectionClass();

            CompositeBinding compositeBinding = module.getCompositeBinding( injectionClass );
            if( compositeBinding != null )
            {
                CompositeResolution compositeResolution = compositeBinding.getCompositeResolution();
                CompositeModel model = compositeResolution.getCompositeModel();
                Class<? extends ServiceComposite> compositeType = (Class<? extends ServiceComposite>) model.getCompositeClass();
                instance = cbf.newCompositeBuilder( compositeType ).newInstance();
            }
            else
            {
                throw new ServiceProviderException( "No Composite type registered which extends the desired service type " + injectionClass.getName() );
            }
        }
        return (T) instance;
    }

    public void releaseService( ServiceComposite service )
    {
        // Ignore for now
    }
}
