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

package org.qi4j.runtime.structure;

import java.lang.reflect.Type;
import java.util.List;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.common.Visibility;

/**
 * TODO
 */
public final class UsedLayersInstance
{
    private final List<LayerInstance> usedLayerInstances;

    public UsedLayersInstance( List<LayerInstance> usedLayerInstances )
    {
        this.usedLayerInstances = usedLayerInstances;
    }

    public ModuleInstance findModuleForComposite( Class mixinType )
    {
        ModuleInstance foundModule = null;
        for( LayerInstance usedLayerInstance : usedLayerInstances )
        {
            ModuleInstance module = usedLayerInstance.findModuleForComposite( mixinType, Visibility.application );
            if( module != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                foundModule = module;
            }
        }

        return foundModule;
    }

    public ModuleInstance findModuleForEntity( Class mixinType )
    {
        ModuleInstance foundModule = null;
        for( LayerInstance usedLayerInstance : usedLayerInstances )
        {
            ModuleInstance module = usedLayerInstance.findModuleForEntity( mixinType, Visibility.application );
            if( module != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                foundModule = module;
            }
        }

        return foundModule;
    }

    public ModuleInstance findModuleForObject( Class type )
    {
        ModuleInstance foundModule = null;
        for( LayerInstance usedLayerInstance : usedLayerInstances )
        {
            ModuleInstance module = usedLayerInstance.findModuleForObject( type, Visibility.application );
            if( module != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( type );
                }
                foundModule = module;
            }
        }

        return foundModule;
    }


    public <T> void getServiceReferencesFor( Type serviceType, List<ServiceReference<T>> serviceReferences )
    {
        for( LayerInstance usedLayerInstance : usedLayerInstances )
        {
            usedLayerInstance.getServiceReferencesFor( serviceType, Visibility.application, serviceReferences );
        }
    }

    public Class getClassForName( String type )
    {
        Class clazz;
        for( LayerInstance usedLayerInstance : usedLayerInstances )
        {
            clazz = usedLayerInstance.getClassForName( type );
            if( clazz != null )
            {
                return clazz;
            }
        }

        return null;
    }
}
