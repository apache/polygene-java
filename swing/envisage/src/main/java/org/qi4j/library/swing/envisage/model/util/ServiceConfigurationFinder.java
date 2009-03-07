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
package org.qi4j.library.swing.envisage.model.util;

import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.EntityDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.LayerDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.library.swing.envisage.model.descriptor.ValueDetailDescriptor;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
class ServiceConfigurationFinder
{
    public Object findConfigurationDescriptor(ServiceDetailDescriptor descriptor)
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
            Object obj = findInServices( descriptor.services(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInEntities( descriptor.entities(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            obj = findInValues( descriptor.values(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }

            // TODO findInTransients

            obj = findInObjects( descriptor.objects(), configType );
            if( obj != null )
            {
                configDescriptor = obj;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInServices( Iterable<ServiceDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ServiceDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }

        return configDescriptor;
    }

    private Object findInEntities( Iterable<EntityDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( EntityDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
    }

    private Object findInValues( Iterable<ValueDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ValueDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
    }

    private Object findInObjects( Iterable<ObjectDetailDescriptor> iter, Class<?> configType )
    {
        Object configDescriptor = null;

        for( ObjectDetailDescriptor descriptor : iter )
        {
            if( configType.equals( descriptor.descriptor().type() ) )
            {
                configDescriptor = descriptor;
                break;
            }
        }


        return configDescriptor;
    }
}
