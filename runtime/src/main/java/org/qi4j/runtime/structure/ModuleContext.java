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

package org.qi4j.runtime.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.composite.ObjectContext;
import org.qi4j.service.ServiceLocator;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ModuleContext
{
    private ModuleBinding moduleBinding;
    private Map<Class, ObjectContext> objectContexts;
    private Map<Class<? extends Composite>, CompositeContext> compositeContexts;
    private Map<Class, Class<? extends Composite>> interfaceCompositeMapping;

    public ModuleContext( ModuleBinding moduleBinding, Map<Class<? extends Composite>, CompositeContext> compositeContexts, Map<Class, ObjectContext> instantiableObjectContexts, Map<Class<? extends Composite>, ModuleContext> moduleContexts )
    {
        this.moduleBinding = moduleBinding;
        objectContexts = instantiableObjectContexts;

        this.compositeContexts = compositeContexts;

        interfaceCompositeMapping = createMapping();
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }

    public ModuleInstance newModuleInstance( Map<Class<? extends Composite>, ModuleInstance> modulesForPublicComposites,
                                             Map<Class, ModuleInstance> modulesForPublicObjects,
                                             ServiceLocator layerServiceLocator )
    {

        ModuleInstance moduleInstance = new ModuleInstance( this, modulesForPublicComposites, modulesForPublicObjects, layerServiceLocator );
        return moduleInstance;
    }

    public CompositeContext getCompositeContext( Class<? extends Composite> compositeType )
    {
        return compositeContexts.get( compositeType );
    }

    public ObjectContext getObjectContext( Class objectType )
    {
        return objectContexts.get( objectType );
    }

    public Map<Class<? extends Composite>, CompositeContext> getCompositeContexts()
    {
        return compositeContexts;
    }

    public Class<? extends Composite> getCompositeForInterface( Class interfaceClass )
    {
        return interfaceCompositeMapping.get( interfaceClass );
    }

    private Map<Class, Class<? extends Composite>> createMapping()
    {
        Map<Class, Class<? extends Composite>> mapping = new HashMap<Class, Class<? extends Composite>>();
        Map<Class<? extends Composite>, CompositeContext> composites = getCompositeContexts();
        for( Class<? extends Composite> compositeType : composites.keySet() )
        {
            mapComposite( compositeType, mapping );
        }
        cleanupDummies( mapping );
        return mapping;
    }

    private void mapComposite( Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        mapType( compositeType, compositeType, mapping );
    }

    private void mapType( Class type, Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        if( mapping.containsKey( type ) )
        {
            mapping.put( type, Composite.class );
        }
        else
        {
            mapping.put( type, compositeType );
        }

        for( Class subtype : type.getInterfaces() )
        {
            mapType( subtype, compositeType, mapping );
        }
    }

    private void cleanupDummies( Map<Class, Class<? extends Composite>> mapping )
    {
        Iterator<Class<? extends Composite>> it = mapping.values().iterator();
        while( it.hasNext() )
        {
            Class<? extends Composite> isDummy = it.next();
            if( isDummy == Composite.class )
            {
                it.remove();
            }
        }
    }

}
