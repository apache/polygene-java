/*  Copyright 2009 Tonny Kohar.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.tools.model.util;

import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.tools.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.tools.model.descriptor.EntityDetailDescriptor;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.tools.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.tools.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.tools.model.descriptor.ValueDetailDescriptor;

import static org.qi4j.functional.Iterables.first;

class ServiceConfigurationFinder
{
    public Object findConfigurationDescriptor( ServiceDetailDescriptor descriptor )
    {
        Class<?> configType = descriptor.descriptor().configurationType();

        if( configType == null )
        {
            return null;
        }

        // traverse the appDescritor to find the configurationDescriptor
        ApplicationDetailDescriptor appDescriptor = descriptor.module().layer().application();
        Object obj = findConfigurationDescriptor( appDescriptor, configType );
        if( obj == null )
        {
            return null;
        }

        return obj;
    }

    private Object findConfigurationDescriptor( ApplicationDetailDescriptor descriptor, Class<?> configType )
    {
        Object configDescriptor = null;
        for( LayerDetailDescriptor childDescriptor : descriptor.layers() )
        {
            Object obj = findInModules( childDescriptor.modules(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInModules( Iterable<ModuleDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ModuleDetailDescriptor descriptor : iter )
        {
            Object obj = findInTypes( descriptor.services(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInTypes( descriptor.entities(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            // findInTransients
            obj = findInTypes( descriptor.composites(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInTypes( descriptor.values(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInTypes( descriptor.objects(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInTypes( Iterable iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( Object obj : iter )
        {
            ModelDescriptor descriptor;

            if( obj instanceof ServiceDetailDescriptor )
            {
                descriptor = ( (ServiceDetailDescriptor) obj ).descriptor();
            }
            else if( obj instanceof EntityDetailDescriptor )
            {
                descriptor = ( (EntityDetailDescriptor) obj ).descriptor();
            }
            else if( obj instanceof ValueDetailDescriptor )
            {
                descriptor = ( (ValueDetailDescriptor) obj ).descriptor();
            }
            else if( obj instanceof ObjectDetailDescriptor )
            {
                descriptor = ( (ObjectDetailDescriptor) obj ).descriptor();
            }
            else
            {
                descriptor = ( (CompositeDetailDescriptor) obj ).descriptor();
            }

            if( configType.equals( first( descriptor.types() ) ) )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }
}
