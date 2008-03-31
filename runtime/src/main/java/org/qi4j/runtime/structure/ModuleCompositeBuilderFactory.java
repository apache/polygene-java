/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime.structure;

import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.composite.AmbiguousMixinTypeException;
import org.qi4j.composite.MixinTypeNotAvailableException;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.structure.ModuleModel;
import org.qi4j.spi.structure.ModuleResolution;

/**
 * Default implementation of CompositeBuilderFactory
 */
public class ModuleCompositeBuilderFactory
    implements CompositeBuilderFactory
{
    private ModuleInstance moduleInstance;

    public ModuleCompositeBuilderFactory( ModuleInstance moduleInstance )
    {
        this.moduleInstance = moduleInstance;
    }

    public <T> CompositeBuilder<T> newCompositeBuilder( Class<T> mixinType )
    {
        validateNotNull( "mixinType", mixinType );
        Class<? extends Composite> compositeType;
        if( ! Composite.class.isAssignableFrom( mixinType ) )
        {
            compositeType = locateCompositeType( mixinType );
        }
        else
        {
            compositeType = (Class<? extends Composite>) mixinType;
        }
        // Find which Module handles this Composite type
        ModuleInstance compositeModuleInstance = moduleInstance.getModuleForComposite( compositeType );

        // Get the Composite context
        ModuleContext context = compositeModuleInstance.getModuleContext();
        CompositeContext compositeContext = context.getCompositeContext( compositeType );

        if( compositeContext == null )
        {
            String compositeTypeName = compositeType.getName();

            ModuleContext moduleContext = moduleInstance.getModuleContext();
            ModuleBinding moduleBinding = moduleContext.getModuleBinding();
            ModuleResolution moduleResolution = moduleBinding.getModuleResolution();
            ModuleModel moduleModel = moduleResolution.getModuleModel();
            String moduleModelName = moduleModel.getName();

            throw new InvalidApplicationException(
                "Trying to create unregistered composite of type [" + compositeTypeName + "] in module [" +
                moduleModelName + "]."
            );
        }

        return createBuilder( compositeModuleInstance, compositeContext );
    }

    private <T> Class<? extends Composite> locateCompositeType( Class<T> mixinType )
    {
        Class<? extends Composite> compositeType;
        ModuleInstance module = moduleInstance.getModuleForMixinType( mixinType );
        if( module == null )
        {
        }
        ModuleContext moduleContext = module.getModuleContext();
        compositeType = moduleContext.getCompositeForMixinType( mixinType );
        if( compositeType == Composite.class )
        {
            // conflict detected earlier.
            throw new AmbiguousMixinTypeException( mixinType );
        }
        if( compositeType == null )
        {
            ModuleBinding moduleBinding = moduleContext.getModuleBinding();
            ModuleResolution moduleResolution = moduleBinding.getModuleResolution();
            ModuleModel moduleModel = moduleResolution.getModuleModel();
            String moduleModelName = moduleModel.getName();

            throw new MixinTypeNotAvailableException( mixinType, moduleModelName );
        }
        return compositeType;
    }

    public <T> T newComposite( Class<T> mixinType )
    {
        return newCompositeBuilder( mixinType ).newInstance();
    }

    protected <T> CompositeBuilder<T> createBuilder( ModuleInstance moduleInstance, CompositeContext compositeContext )
    {
        // Create a builder
        return new CompositeBuilderImpl<T>( moduleInstance, compositeContext );
    }
}