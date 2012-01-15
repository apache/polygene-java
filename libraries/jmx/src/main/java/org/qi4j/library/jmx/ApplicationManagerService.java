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

import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.*;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.structure.*;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Iterables;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Expose the Qi4j app as a "tree" of MBeans.
 *
 * Other services should reuse the object names and create
 * nodes under the ones created here. For example:
 * <pre>
 * Qi4j:application=MyApp,layer=Application,module=MyModule,class=Service,service=MyService
 * </pre>
 * is exported by this service, so another exporter showing some aspect related to this service should
 * use this as base for the ObjectName, and add their own properties. Example:
 * <pre>
 * Qi4j:application=MyApp,layer=Application,module=MyModule,class=Service,service=MyService,name=Configuration
 * </pre>
 * Use the following snippet to find the ObjectName of a service with a given identity:
 * <pre>
 * ObjectName serviceName = Qi4jMBeans.findService(mbeanServer, applicationName, serviceId);
 * </pre>
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
        public Application application;

        private List<ObjectName> mbeans = new ArrayList<ObjectName>( );

        public void activate() throws Exception
        {
            application.descriptor().accept( new HierarchicalVisitorAdapter<Object, Object, Exception>()
            {
                Layer layer;
                Module module;
                Stack<ObjectName> names = new Stack<ObjectName>();

                @Override
                public boolean visitEnter( Object visited ) throws Exception
                {
                    if (visited instanceof LayerDescriptor)
                    {
                        LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
                        layer = application.findLayer( layerDescriptor.name() );

                        LayerBean layerBean = new LayerBean(layer, layerDescriptor);
                        ObjectName objectName = new ObjectName( "Qi4j:application="+application.name()+",layer="+layer.name() );
                        names.push( objectName );

                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, layerDescriptor.name(), LayerBean.class.getName()).
                                attribute( "uses", "Layer usages", String.class.getName(), "Other layers that this layer uses", "getUses", null ).
                                operation( "restart", "Restart layer", String.class.getName(), MBeanOperationInfo.ACTION_INFO ).
                                newModelMBean();

                        mbean.setManagedResource( layerBean, "ObjectReference" );
                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    } else if (visited instanceof ModuleDescriptor)
                    {
                        ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
                        module = application.findModule( layer.name(), moduleDescriptor.name() );
                        ObjectName objectName = new ObjectName( names.peek().toString()+",module="+moduleDescriptor.name() );
                        names.push( objectName );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, moduleDescriptor.name(), moduleDescriptor.getClass().getName()).
                                attribute( "name", "Module name", String.class.getName(), "Name of module", "name", null ).
                                newModelMBean();

                        mbean.setManagedResource( moduleDescriptor, "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    } else if (visited instanceof ServiceDescriptor)
                    {
                        ServiceDescriptor serviceDescriptor = (ServiceDescriptor) visited;
                        ObjectName objectName = new ObjectName( names.peek().toString()+",class=Service,service="+serviceDescriptor.identity() );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, serviceDescriptor.identity(), ServiceBean.class.getName()).
                                attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                                attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null ).
                                attribute( "Type", "Service type", String.class.getName(), "Type of service", "getType", null ).
                                attribute( "Active", "Service is active", Boolean.class.getName(), "Service is active", "isActive", null ).
                                attribute( "Available", "Service is available", Boolean.class.getName(), "Service is available", "isAvailable", null ).
                                operation( "restart", "Restart service", String.class.getName(), ModelMBeanOperationInfo.ACTION_INFO).
                                newModelMBean();

                        mbean.setManagedResource( new ServiceBean(serviceDescriptor, module), "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    } else if (visited instanceof ImportedServiceDescriptor)
                    {
                        ImportedServiceDescriptor importedServiceDescriptor = (ImportedServiceDescriptor) visited;
                        ObjectName objectName = new ObjectName( names.peek().toString()+",class=Imported service,importedservice="+importedServiceDescriptor.identity() );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, importedServiceDescriptor.identity(), ImportedServiceBean.class.getName()).
                                attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                                attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null ).
                                attribute( "Type", "Service type", String.class.getName(), "Type of imported service", "getType", null ).
                                newModelMBean();

                        mbean.setManagedResource( new ImportedServiceBean(importedServiceDescriptor), "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    }

                    return !(visited instanceof ModelDescriptor);
                }

                @Override
                public boolean visitLeave( Object visited ) throws Exception
                {
                    if (visited instanceof ModuleDescriptor || visited instanceof LayerDescriptor)
                        names.pop();

                    return true;
                }
            } );
        }

        public void passivate() throws Exception
        {
            for (ObjectName mbean : mbeans)
            {
                server.unregisterMBean( mbean );
            }
        }
    }

    public static class LayerBean
    {
        private final Layer layer;
        private final LayerDescriptor layerDescriptor;
        private String uses;

        public LayerBean( Layer layer, LayerDescriptor layerDescriptor)
        {
            this.layer = layer;
            this.layerDescriptor = layerDescriptor;

            uses = "Uses: ";
            for (LayerDescriptor usedLayer : layerDescriptor.usedLayers().layers())
            {
                uses +=usedLayer.name()+" ";
            }
        }

        public String getUses()
        {
            return uses;
        }

        public String restart()
            throws Exception
        {
            try
            {
                layer.passivate();
                layer.activate();
                return "Restarted layer";
            } catch (Exception e)
            {
                return "Could not restart layer:"+e.getMessage();
            }
        }
    }

    public static class ServiceBean
    {
        private final ServiceDescriptor serviceDescriptor;
        private final Module module;

        public ServiceBean( ServiceDescriptor serviceDescriptor, Module module )
        {
            this.serviceDescriptor = serviceDescriptor;
            this.module = module;
        }

        public String getId()
        {
            return serviceDescriptor.identity();
        }

        public String getVisibility()
        {
            return serviceDescriptor.visibility().name();
        }

        public String getType()
        {
            return serviceDescriptor.type().getName();
        }

        public boolean isActive()
        {
            return Iterables.first( Iterables.filter( ServiceQualifier.withId( serviceDescriptor.identity() ), module.findServices( serviceDescriptor.type() ))).isActive();
        }

        public boolean isAvailable()
        {
            return Iterables.first( Iterables.filter( ServiceQualifier.withId( serviceDescriptor.identity() ), module.findServices( serviceDescriptor.type() ))).isAvailable();
        }

        public String restart()
        {
            Iterable services = module.findServices( Activatable.class );
            ServiceReference<Activatable> serviceRef = (ServiceReference<Activatable>) Iterables.first( Iterables.filter( ServiceQualifier.withId( serviceDescriptor.identity() ), services ));
            if (serviceRef != null)
            {
                try
                {
                    ((Activatable)serviceRef).passivate();
                    ((Activatable)serviceRef).activate();
                    return "Restarted service";
                } catch (Exception e)
                {
                    return "Could not restart service:"+e.getMessage();
                }
            } else
                return "Could not find service";
        }
    }

    public static class ImportedServiceBean
    {
        private final ImportedServiceDescriptor serviceDescriptor;

        public ImportedServiceBean( ImportedServiceDescriptor serviceDescriptor)
        {
            this.serviceDescriptor = serviceDescriptor;
        }

        public String getId()
        {
            return serviceDescriptor.identity();
        }

        public String getVisibility()
        {
            return serviceDescriptor.visibility().name();
        }

        public String getType()
        {
            return serviceDescriptor.type().getName();
        }
    }
}
