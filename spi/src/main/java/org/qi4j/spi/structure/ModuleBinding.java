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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.ObjectBinding;

/**
 * TODO
 */
public class ModuleBinding
    implements Serializable
{
    private ModuleResolution moduleResolution;
    private Map<Class, ObjectBinding> objectBindings;

    private Map<Class<? extends Composite>, CompositeBinding> compositeBindings;

    public ModuleBinding( ModuleResolution moduleResolution, Map<Class<? extends Composite>, CompositeBinding> compositeBindingMap, Map<Class, ObjectBinding> objectBindings )
    {
        this.compositeBindings = compositeBindingMap;
        this.moduleResolution = moduleResolution;
        this.objectBindings = objectBindings;
    }

    public ModuleResolution getModuleResolution()
    {
        return moduleResolution;
    }

    public Map<Class<? extends Composite>, CompositeBinding> getCompositeBindings()
    {
        return compositeBindings;
    }

    public CompositeBinding getCompositeBinding( Class type )
    {
        for( Map.Entry<Class<? extends Composite>, CompositeBinding> entry : compositeBindings.entrySet() )
        {
            if( type.isAssignableFrom( entry.getKey() ) )
            {
                return entry.getValue();
            }
        }

        return null; // No Composite bound which matches this type
    }

    public Map<Class, ObjectBinding> getObjectBindings()
    {
        return objectBindings;
    }
}
