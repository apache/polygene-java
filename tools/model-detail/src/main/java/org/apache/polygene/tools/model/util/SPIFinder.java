/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.tools.model.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.tools.model.descriptor.ApplicationDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.EntityDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.InjectedFieldDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.LayerDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.MixinDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.ModuleDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.ObjectDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.ServiceDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.TransientDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.ValueDetailDescriptor;

/**
 * SPI would be defined as "All service dependencies which are not satisfied from within the module or Layer".
 */
class SPIFinder
{
    private ApplicationDetailDescriptor appDetailDescriptor;

    public List<ServiceDetailDescriptor> findModule( ModuleDetailDescriptor descriptor )
    {
        appDetailDescriptor = descriptor.layer().application();

        ArrayList<ServiceDetailDescriptor> list = new ArrayList<>();

        findInServices( descriptor.services(), list );
        findInEntities( descriptor.entities(), list );
        findInValues( descriptor.values(), list );
        findInTransients( descriptor.transients(), list );
        findInObjects( descriptor.objects(), list );

        return list;
    }

    public List<ServiceDetailDescriptor> findLayerSPI( LayerDetailDescriptor descriptor )
    {
        List<ServiceDetailDescriptor> list = new ArrayList<>();

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

    private void findInTransients( Iterable<TransientDetailDescriptor> iter, ArrayList<ServiceDetailDescriptor> list )
    {
        for( TransientDetailDescriptor descriptor : iter )
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
