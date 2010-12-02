/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.jmx;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * JAVADOC
 */
@Mixins(ApplicationManagerService.Mixin.class)
public interface ApplicationManagerService
    extends ServiceComposite, Activatable
{
    class Mixin
        implements Activatable
    {
        @Service
        public MBeanServer server;

        @Structure
        public ApplicationSPI application;

        public void activate() throws Exception
        {
            application.visitDescriptor( new DescriptorVisitor<Exception>()
            {
                String applicationName;
                String layerName;
                String moduleName;

                @Override
                public void visit( ApplicationDescriptor applicationDescriptor ) throws Exception
                {
                    applicationName = applicationDescriptor.name();
                }

                @Override
                public void visit( LayerDescriptor layerDescriptor ) throws Exception
                {
                    layerName = layerDescriptor.name();
                }

                @Override
                public void visit( ModuleDescriptor moduleDescriptor ) throws Exception
                {
                    moduleName = moduleDescriptor.name();
                    ObjectName objectName = new ObjectName( "Qi4j:application="+applicationName+",layer="+layerName+",module="+moduleDescriptor.name() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, moduleDescriptor.name(), moduleDescriptor.getClass().getName()).
                            attribute( "Name", "Module name", String.class.getName(), "Name of module", "name", null ).
                            newModelMBean();

                    mbean.setManagedResource( moduleDescriptor, "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                }

                @Override
                public void visit( ServiceDescriptor serviceDescriptor ) throws Exception
                {
                    ObjectName objectName = new ObjectName( "Qi4j:application="+applicationName+",layer="+layerName+",module="+moduleName+",service="+serviceDescriptor.identity() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, serviceDescriptor.identity(), ServiceBean.class.getName()).
                            attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                            attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null ).
                            operation( "restart", "Restart service", "void", ModelMBeanOperationInfo.ACTION).
                            newModelMBean();

                    mbean.setManagedResource( new ServiceBean(serviceDescriptor, layerName, moduleName, Mixin.this), "objectReference" );

                    server.registerMBean( mbean, objectName );
                    
                }
            });
        }

        public void passivate() throws Exception
        {
        }
    }

    public static class ServiceBean
    {
        private final ServiceDescriptor serviceDescriptor;
        private final String layerName;
        private final String moduleName;
        private final Mixin appManager;

        public ServiceBean( ServiceDescriptor serviceDescriptor, String layerName, String moduleName, Mixin appManager )
        {
            this.serviceDescriptor = serviceDescriptor;
            this.layerName = layerName;
            this.moduleName = moduleName;
            this.appManager = appManager;
        }

        public String getId()
        {
            return serviceDescriptor.identity();
        }

        public String getVisibility()
        {
            return serviceDescriptor.visibility().name();
        }

        public void restart() throws Exception
        {
            Iterable services = appManager.application.findModule( layerName, moduleName ).serviceFinder().findServices( Activatable.class );
            ServiceReference<Activatable> serviceRef = (ServiceReference<Activatable>) Iterables.first(Iterables.filter( ServiceQualifier.withId( serviceDescriptor.identity() ), services ));
            if (serviceRef != null)
            {
                ((Activatable)serviceRef).passivate();
                ((Activatable)serviceRef).activate();
            }
        }
    }
}
