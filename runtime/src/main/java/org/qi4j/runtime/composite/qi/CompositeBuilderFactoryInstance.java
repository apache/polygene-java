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

package org.qi4j.runtime.composite.qi;

import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.CompositeNotRegisteredException;
import org.qi4j.composite.InstantiationException;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public final class CompositeBuilderFactoryInstance
    implements CompositeBuilderFactory
{
    private ModuleInstance module;

    public CompositeBuilderFactoryInstance( ModuleInstance module )
    {
        this.module = module;
    }

    public <T> CompositeBuilder<T> newCompositeBuilder( Class<T> mixinType ) throws InvalidApplicationException
    {
        ModuleInstance compositeModuleInstance = module.findModuleFor( mixinType );

        // Get the Composite context
        if( compositeModuleInstance != null )
        {
            CompositeBuilder builder = new CompositeBuilderInstance( compositeModuleInstance, compositeModuleInstance.model().getCompositeModelFor( mixinType ) );
            return builder;
        }
        else
        {
            throw new CompositeNotRegisteredException( mixinType, module.model().name() );
        }
    }

    public <T> T newComposite( Class<T> compositeType ) throws InvalidApplicationException, InstantiationException
    {
        return newCompositeBuilder( compositeType ).newInstance();
    }
}
