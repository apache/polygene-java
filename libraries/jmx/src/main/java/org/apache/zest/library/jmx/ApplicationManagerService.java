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

package org.apache.zest.library.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import org.apache.zest.api.activation.Activation;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.LayerDescriptor;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.functional.HierarchicalVisitorAdapter;

import static org.apache.zest.api.service.qualifier.ServiceQualifier.withId;
import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.first;

/**
 * Expose the Zest app as a "tree" of MBeans.
 *
 * Other services should reuse the object names and create
 * nodes under the ones created here. For example:
 * <pre>
 * Zest:application=MyApp,layer=Application,module=MyModule,class=Service,service=MyService
 * </pre>
 * is exported by this service, so another exporter showing some aspect related to this service should
 * use this as base for the ObjectName, and add their own properties. Example:
 * <pre>
 * Zest:application=MyApp,layer=Application,module=MyModule,class=Service,service=MyService,name=Configuration
 * </pre>
 * Use the following snippet to find the ObjectName of a service with a given identity:
 * <pre>
 * ObjectName serviceName = ZestMBeans.findService(mbeanServer, applicationName, serviceId);
 * </pre>
 */
@Mixins( ApplicationManagerService.Mixin.class )
@Activators( ApplicationManagerService.Activator.class )
public interface ApplicationManagerService
    extends ServiceComposite
{

    void exportApplicationStructure()
        throws Exception;

    void unexportApplicationStructure()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<ApplicationManagerService>>
    {

        @Override
        public void afterActivation( ServiceReference<ApplicationManagerService> activated )
            throws Exception
        {
            activated.get().exportApplicationStructure();
        }

        @Override
        public void beforePassivation( ServiceReference<ApplicationManagerService> passivating )
            throws Exception
        {
            passivating.get().unexportApplicationStructure();
        }
    }

    abstract class Mixin
        implements ApplicationManagerService
    {
        @Service
        public MBeanServer server;

        @Structure
        public Application application;

        private List<ObjectName> mbeans = new ArrayList<>();

        @Override
        public void exportApplicationStructure()
            throws Exception
        {
            application.descriptor().accept( new HierarchicalVisitorAdapter<Object, Object, Exception>()
            {
                Layer layer;
                Module module;
                Stack<ObjectName> names = new Stack<>();

                @Override
                public boolean visitEnter( Object visited )
                    throws Exception
                {
                    if( visited instanceof LayerDescriptor )
                    {
                        LayerDescriptor layerDescriptor = (LayerDescriptor) visited;
                        layer = application.findLayer( layerDescriptor.name() );

                        LayerBean layerBean = new LayerBean( layer, layerDescriptor );
                        ObjectName objectName = new ObjectName( "Zest:application=" + application.name() + ",layer=" + layer
                            .name() );
                        names.push( objectName );

                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, layerDescriptor.name(), LayerBean.class
                            .getName() ).
                            attribute( "uses", "Layer usages", String.class.getName(), "Other layers that this layer uses", "getUses", null )
                            .operation( "restart", "Restart layer", String.class.getName(), MBeanOperationInfo.ACTION_INFO )
                            .newModelMBean();

                        mbean.setManagedResource( layerBean, "ObjectReference" );
                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    }
                    else if( visited instanceof ModuleDescriptor )
                    {
                        ModuleDescriptor moduleDescriptor = (ModuleDescriptor) visited;
                        module = application.findModule( layer.name(), moduleDescriptor.name() );
                        ObjectName objectName = new ObjectName( names.peek()
                                                                    .toString() + ",module=" + moduleDescriptor.name() );
                        names.push( objectName );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, moduleDescriptor.name(), moduleDescriptor
                            .getClass()
                            .getName() ).
                            attribute( "name", "Module name", String.class.getName(), "Name of module", "name", null ).
                            newModelMBean();

                        mbean.setManagedResource( moduleDescriptor, "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    }
                    else if( visited instanceof ServiceDescriptor )
                    {
                        ServiceDescriptor serviceDescriptor = (ServiceDescriptor) visited;
                        ObjectName objectName = new ObjectName( names.peek()
                                                                    .toString() + ",class=Service,service=" + serviceDescriptor
                                                                    .identity() );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, serviceDescriptor.identity(), ServiceBean.class
                            .getName() ).
                            attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                            attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null )
                            .
                                attribute( "Type", "Service type", String.class.getName(), "Type of service", "getType", null )
                            .
                                attribute( "Active", "Service is active", Boolean.class.getName(), "Service is active", "isActive", null )
                            .
                                attribute( "Available", "Service is available", Boolean.class.getName(), "Service is available", "isAvailable", null )
                            .
                                operation( "restart", "Restart service", String.class.getName(), ModelMBeanOperationInfo.ACTION_INFO )
                            .
                                newModelMBean();

                        mbean.setManagedResource( new ServiceBean( serviceDescriptor, module ), "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    }
                    else if( visited instanceof ImportedServiceDescriptor )
                    {
                        ImportedServiceDescriptor importedServiceDescriptor = (ImportedServiceDescriptor) visited;
                        ObjectName objectName = new ObjectName( names.peek()
                                                                    .toString() + ",class=Imported service,importedservice=" + importedServiceDescriptor
                                                                    .identity() );
                        RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, importedServiceDescriptor.identity(), ImportedServiceBean.class
                            .getName() ).
                            attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                            attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null )
                            .
                                attribute( "Type", "Service type", String.class.getName(), "Type of imported service", "getType", null )
                            .
                                newModelMBean();

                        mbean.setManagedResource( new ImportedServiceBean( importedServiceDescriptor ), "ObjectReference" );

                        server.registerMBean( mbean, objectName );
                        mbeans.add( objectName );
                    }

                    return !( visited instanceof ModelDescriptor );
                }

                @Override
                public boolean visitLeave( Object visited )
                    throws Exception
                {
                    if( visited instanceof ModuleDescriptor || visited instanceof LayerDescriptor )
                    {
                        names.pop();
                    }

                    return true;
                }
            } );
        }

        @Override
        public void unexportApplicationStructure()
            throws Exception
        {
            for( ObjectName mbean : mbeans )
            {
                server.unregisterMBean( mbean );
            }
        }
    }

    class LayerBean
    {
        private final Layer layer;
        private final LayerDescriptor layerDescriptor;
        private String uses;

        public LayerBean( Layer layer, LayerDescriptor layerDescriptor )
        {
            this.layer = layer;
            this.layerDescriptor = layerDescriptor;

            uses = "Uses: ";
            for( LayerDescriptor usedLayer : layerDescriptor.usedLayers().layers() )
            {
                uses += usedLayer.name() + " ";
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
            }
            catch( Exception e )
            {
                return "Could not restart layer:" + e.getMessage();
            }
        }
    }

    class ServiceBean
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
            Class<?> first = first( serviceDescriptor.types() );
            if( first == null )
            {
                return null;
            }
            return first.getName();
        }

        public boolean isActive()
        {
            Class<?> mainType = first( serviceDescriptor.types() );
            ServiceReference<?> first = first( filter( withId( serviceDescriptor.identity() ),
                                                       module.findServices( mainType ) )
            );
            return first != null && first.isActive();
        }

        public boolean isAvailable()
        {
            Class<?> mainType = first( serviceDescriptor.types() );
            ServiceReference<?> first = first( filter( withId( serviceDescriptor.identity() ),
                                                       module.findServices( mainType ) )
            );
            return first != null && first.isAvailable();
        }

        public String restart()
        {
            Iterable<?> services = module.findServices( first( serviceDescriptor.types() ) );
            ServiceReference<?> serviceRef = (ServiceReference) first( filter( withId( serviceDescriptor.identity() ),
                                                                               services )
            );
            if( serviceRef != null )
            {
                try
                {
                    ( (Activation) serviceRef ).passivate();
                    ( (Activation) serviceRef ).activate();
                    return "Restarted service";
                }
                catch( Exception e )
                {
                    return "Could not restart service:" + e.getMessage();
                }
            }
            else
            {
                return "Could not find service";
            }
        }
    }

    class ImportedServiceBean
    {
        private final ImportedServiceDescriptor serviceDescriptor;

        public ImportedServiceBean( ImportedServiceDescriptor serviceDescriptor )
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
            Class<?> mainType = first( serviceDescriptor.types() );
            if( mainType == null )
            {
                return null;
            }
            return mainType.getName();
        }
    }
}
