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

import java.util.ArrayList;
import java.util.List;
import org.qi4j.tools.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.tools.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.tools.model.descriptor.EntityDetailDescriptor;
import org.qi4j.tools.model.descriptor.InjectedFieldDetailDescriptor;
import org.qi4j.tools.model.descriptor.LayerDetailDescriptor;
import org.qi4j.tools.model.descriptor.MixinDetailDescriptor;
import org.qi4j.tools.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.tools.model.descriptor.ObjectDetailDescriptor;
import org.qi4j.tools.model.descriptor.ServiceDetailDescriptor;
import org.qi4j.tools.model.descriptor.ValueDetailDescriptor;

/**
 * SPI would be defined as "All service dependencies which
 * are not satisfied from within the module or Layer".
 */
class SPIFinder
{

    private ApplicationDetailDescriptor appDetailDescriptor;

    public List<ServiceDetailDescriptor> findModule( ModuleDetailDescriptor descriptor )
    {
        appDetailDescriptor = descriptor.layer().application();

        ArrayList<ServiceDetailDescriptor> list = new ArrayList<ServiceDetailDescriptor>();

        findInServices( descriptor.services(), list );
        findInEntities( descriptor.entities(), list );
        findInValues( descriptor.values(), list );
        findInTransients( descriptor.composites(), list );
        findInObjects( descriptor.objects(), list );

        return list;
    }

    public List<ServiceDetailDescriptor> findLayerSPI( LayerDetailDescriptor descriptor )
    {
        ArrayList<ServiceDetailDescriptor> list = new ArrayList<ServiceDetailDescriptor>();

        for( ModuleDetailDescriptor moduleDetailDescriptor : descriptor.modules() )
        {
            list.addAll( findModule( moduleDetailDescriptor ) );
        }

        return list;
    }

    private void findInMixin( Iterable<MixinDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( MixinDetailDescriptor descriptor : iter )
        {
            findInInjectedField( descriptor.injectedFields(), list );
        }
    }

    private void findInInjectedField( Iterable<InjectedFieldDetailDescriptor> iter,
                                      ArrayList<ServiceDetailDescriptor> list
    )
    {
//        for( InjectedFieldDetailDescriptor descriptorField : iter )
//        {
//            DependencyDescriptor dependencyDescriptor = descriptorField.descriptor().dependency();
//            Annotation annotation = dependencyDescriptor.injectionAnnotation();

//            Class<? extends Annotation> clazz = annotation.annotationType();
//            if( Uses.class.equals( clazz ) || Service.class.equals( clazz ) )
//            {
//                for( String name : dependencyDescriptor.injectedServices() )
//                {
//                    ServiceDetailDescriptor serviceDetailDescriptor = lookupServiceDetailDescriptor( name );
//                    if( serviceDetailDescriptor != null )
//                    {
//                        if( !list.contains( serviceDetailDescriptor ) )
//                        {
//                            list.add( serviceDetailDescriptor );
//                        }
//                    }
//                }
//            }
//        }
    }

    private void findInServices( Iterable<ServiceDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( ServiceDetailDescriptor descriptor : iter )
        {
            findInMixin( descriptor.mixins(), list );
        }
    }

    private void findInEntities( Iterable<EntityDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( EntityDetailDescriptor descriptor : iter )
        {
            findInMixin( descriptor.mixins(), list );
        }
    }

    private void findInValues( Iterable<ValueDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( ValueDetailDescriptor descriptor : iter )
        {
            findInMixin( descriptor.mixins(), list );
        }
    }

    private void findInTransients( Iterable<CompositeDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( CompositeDetailDescriptor descriptor : iter )
        {
            findInMixin( descriptor.mixins(), list );
        }
    }

    private void findInObjects( Iterable<ObjectDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( ObjectDetailDescriptor descriptor : iter )
        {
            findInInjectedField( descriptor.injectedFields(), list );
        }
    }

    private ServiceDetailDescriptor lookupServiceDetailDescriptor( String name )
    {
        ServiceDetailDescriptor serviceDetailDescriptor = null;
        for( LayerDetailDescriptor layer : appDetailDescriptor.layers() )
        {
            for( ModuleDetailDescriptor module : layer.modules() )
            {
                for( ServiceDetailDescriptor service : module.services() )
                {
                    if( service.toString().equals( name ) )
                    {
                        serviceDetailDescriptor = service;
                        break;
                    }
                }
                if( serviceDetailDescriptor != null )
                {
                    break;
                }
            }
            if( serviceDetailDescriptor != null )
            {
                break;
            }
        }

        return serviceDetailDescriptor;
    }
}
