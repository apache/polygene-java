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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.AmbiguousTypeException;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.service.Activator;
import org.qi4j.structure.Layer;
import org.qi4j.structure.Module;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class LayerInstance
    implements Layer, Activatable
{
    private final LayerModel model;
    private final ApplicationInstance applicationInstance;
    private final List<ModuleInstance> moduleInstances;
    private final Activator moduleActivator;
    private final UsedLayersInstance usedLayersInstance;

    public LayerInstance( LayerModel model, ApplicationInstance applicationInstance, List<ModuleInstance> moduleInstances, UsedLayersInstance usedLayersInstance )
    {
        this.model = model;
        this.applicationInstance = applicationInstance;
        this.moduleInstances = moduleInstances;
        this.usedLayersInstance = usedLayersInstance;
        this.moduleActivator = new Activator();
    }

    public LayerModel model()
    {
        return model;
    }

    public ApplicationInstance applicationInstance()
    {
        return applicationInstance;
    }

    public String name()
    {
        return model.name();
    }

    public List<Module> modules()
    {
        List<Module> result = new ArrayList<Module>();
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            result.add( moduleInstance );
        }
        return result;
    }

    public UsedLayersInstance usedLayersInstance()
    {
        return usedLayersInstance;
    }

    public ModuleInstance findModuleForComposite( Class mixinType, Visibility visibility )
    {
        // Check this layer
        ModuleInstance foundModule = null;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            CompositeModel compositeModel = moduleInstance.composites().model().getCompositeModelFor( mixinType, visibility );
            if( compositeModel != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                else
                {
                    foundModule = moduleInstance;
                }
            }
        }

        if( foundModule != null )
        {
            return foundModule;
        }

        if( visibility == Visibility.layer )
        {
            // Check application scope
            foundModule = findModuleForComposite( mixinType, Visibility.application );
            if( foundModule != null )
            {
                return foundModule;
            }
            else
            {
                return usedLayersInstance.findModuleForComposite( mixinType );
            }
        }
        else
        {
            return null;
        }
    }

    public ModuleInstance findModuleForEntity( Class mixinType, Visibility visibility )
    {
        // Check this layer
        ModuleInstance foundModule = null;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            EntityModel entityModel = moduleInstance.entities().model().getEntityModelFor( mixinType, visibility );
            if( entityModel != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( mixinType );
                }
                else
                {
                    foundModule = moduleInstance;
                }
            }
        }

        if( foundModule != null )
        {
            return foundModule;
        }

        if( visibility == Visibility.layer )
        {
            // Check application scope
            foundModule = findModuleForEntity( mixinType, Visibility.application );
            if( foundModule != null )
            {
                return foundModule;
            }
            else
            {
                return usedLayersInstance.findModuleForEntity( mixinType );
            }
        }
        else
        {
            return null;
        }
    }


    public ModuleInstance findModuleForObject( Class type, Visibility visibility )
    {
        // Check this layer
        ModuleInstance foundModule = null;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            ObjectModel objectModel = moduleInstance.objects().model().getObjectModelFor( type, visibility );
            if( objectModel != null )
            {
                if( foundModule != null )
                {
                    throw new AmbiguousTypeException( type );
                }
                else
                {
                    foundModule = moduleInstance;
                }
            }
        }

        if( foundModule != null )
        {
            return foundModule;
        }

        if( visibility == Visibility.layer )
        {
            // Check application scope
            foundModule = findModuleForObject( type, Visibility.application );
            if( foundModule != null )
            {
                return foundModule;
            }
            else
            {
                return usedLayersInstance.findModuleForObject( type );
            }
        }
        else
        {
            return null;
        }
    }

    public ModuleInstance findModule( String moduleName )
    {
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            if( moduleInstance.model().name().equals( moduleName ) )
            {
                return moduleInstance;
            }
        }

        return null;
    }

    public void activate() throws Exception
    {
        moduleActivator.activate( moduleInstances );
    }

    public void passivate() throws Exception
    {
        moduleActivator.passivate();
    }

    public Class findClassForName( String type )
    {
        Class clazz = getClassForName( type );

        if( clazz == null )
        {
            clazz = usedLayersInstance.getClassForName( type );
        }

        return clazz;
    }

    public Class getClassForName( String type )
    {
        Class clazz;
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            clazz = moduleInstance.getClassForName( type );
            if( clazz != null )
            {
                return clazz;
            }
        }

        return null;
    }

    @Override public String toString()
    {
        return model.toString();
    }

    public <T> void getServiceReferencesFor( Class<T> serviceType, Visibility visibility, List<ServiceReference<T>> serviceReferences )
    {
        // Check this layer
        for( ModuleInstance moduleInstance : moduleInstances )
        {
            moduleInstance.services().getServiceReferencesFor( serviceType, visibility, serviceReferences );
        }

        if( visibility == Visibility.layer )
        {
            // Check application scope
            for( ModuleInstance moduleInstance : moduleInstances )
            {
                moduleInstance.services().getServiceReferencesFor( serviceType, Visibility.application, serviceReferences );
            }

            usedLayersInstance.getServiceReferencesFor( serviceType, serviceReferences );
        }
    }
}
