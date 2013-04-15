/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.sql.jmx;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.sql.DataSource;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.spi.Qi4jSPI;

/**
 * Expose DataSourceConfiguration through JMX.
 * Allow configurations to be edited, and the services to be restarted.
 */
@Mixins( DataSourceConfigurationManagerService.Mixin.class )
@Activators( DataSourceConfigurationManagerService.Activator.class )
public interface DataSourceConfigurationManagerService
        extends ServiceComposite
{

    void exportDataSources()
            throws Exception;

    void unexportDataSources()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<DataSourceConfigurationManagerService>>
    {

        @Override
        public void afterActivation( ServiceReference<DataSourceConfigurationManagerService> activated )
                throws Exception
        {
            activated.get().exportDataSources();
        }

        @Override
        public void beforePassivation( ServiceReference<DataSourceConfigurationManagerService> passivating )
                throws Exception
        {
            passivating.get().unexportDataSources();
        }

    }

    abstract class Mixin
            implements DataSourceConfigurationManagerService
    {

        @Structure
        Module module;

        @Service
        MBeanServer server;

        @Structure
        Qi4jSPI spi;

        @Structure
        Application application;

        @Service
        Iterable<ServiceReference<DataSource>> dataSources;

        @Service
        ServiceReference<ServiceImporter<DataSource>> dataSourceService;

        private List<ObjectName> configurationNames = new ArrayList<ObjectName>();

        @Override
        public void exportDataSources()
                throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException
        {
            for ( ServiceReference<DataSource> dataSource : dataSources ) {
                String name = dataSource.identity();
                Module module = ( Module ) spi.moduleOf( dataSource );
                EntityDescriptor descriptor = module.entityDescriptor( DataSourceConfiguration.class.getName() );
                List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
                Map<String, AccessibleObject> properties = new LinkedHashMap<String, AccessibleObject>();
                for ( PropertyDescriptor persistentProperty : descriptor.state().properties() ) {
                    if ( !persistentProperty.isImmutable() ) {
                        String propertyName = persistentProperty.qualifiedName().name();
                        String type = persistentProperty.valueType().mainType().getName();
                        attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ) ) );
                        properties.put( propertyName, persistentProperty.accessor() );
                    }
                }

                List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
                operations.add( new MBeanOperationInfo( "restart", "Restart DataSource", new MBeanParameterInfo[ 0 ], "void", MBeanOperationInfo.ACTION_INFO ) );

                MBeanInfo mbeanInfo = new MBeanInfo( DataSourceConfiguration.class.getName(), name, attributes.toArray( new MBeanAttributeInfo[ attributes.size() ] ), null, operations.toArray( new MBeanOperationInfo[ operations.size() ] ), null );
                Object mbean = new ConfigurableDataSource( dataSourceService, mbeanInfo, name, properties );
                ObjectName configurableDataSourceName = new ObjectName( "Qi4j:application=" + application.name() + ",class=Datasource,name=" + name );
                server.registerMBean( mbean, configurableDataSourceName );
                configurationNames.add( configurableDataSourceName );
            }
        }

        @Override
        public void unexportDataSources()
                throws Exception
        {
            for ( ObjectName configurableServiceName : configurationNames ) {
                server.unregisterMBean( configurableServiceName );
            }
        }

        abstract class EditableConfiguration
                implements DynamicMBean
        {

            MBeanInfo info;

            String identity;

            Map<String, AccessibleObject> propertyNames;

            EditableConfiguration( MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
            {
                this.info = info;
                this.identity = identity;
                this.propertyNames = propertyNames;
            }

            @Override
            public Object getAttribute( String name )
                    throws AttributeNotFoundException, MBeanException, ReflectionException
            {
                UnitOfWork uow = module.newUnitOfWork();
                try {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = spi.stateOf( configuration );
                    AccessibleObject accessor = propertyNames.get( name );
                    Property<Object> property = state.propertyFor( accessor );
                    return property.get();
                } catch ( Exception ex ) {
                    throw new ReflectionException( ex, "Could not get attribute " + name );
                } finally {
                    uow.discard();
                }
            }

            @Override
            public void setAttribute( Attribute attribute )
                    throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
            {
                UnitOfWork uow = module.newUnitOfWork();
                try {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = spi.stateOf( configuration );
                    AccessibleObject accessor = propertyNames.get( attribute.getName() );
                    Property<Object> property = state.propertyFor( accessor );
                    property.set( attribute.getValue() );
                    try {
                        uow.complete();
                    } catch ( UnitOfWorkCompletionException e ) {
                        throw new ReflectionException( e );
                    }
                } finally {
                    uow.discard();
                }
            }

            @Override
            public AttributeList getAttributes( String[] names )
            {
                AttributeList list = new AttributeList();
                for ( String name : names ) {
                    try {
                        Object value = getAttribute( name );
                        list.add( new Attribute( name, value ) );
                    } catch ( AttributeNotFoundException e ) {
                        e.printStackTrace();
                    } catch ( MBeanException e ) {
                        e.printStackTrace();
                    } catch ( ReflectionException e ) {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            @Override
            public AttributeList setAttributes( AttributeList attributeList )
            {
                AttributeList list = new AttributeList();
                for ( int i = 0; i < list.size(); i++ ) {
                    Attribute attribute = ( Attribute ) list.get( i );

                    try {
                        setAttribute( attribute );
                        list.add( attribute );
                    } catch ( AttributeNotFoundException e ) {
                        e.printStackTrace();
                    } catch ( InvalidAttributeValueException e ) {
                        e.printStackTrace();
                    } catch ( MBeanException e ) {
                        e.printStackTrace();
                    } catch ( ReflectionException e ) {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            @Override
            public MBeanInfo getMBeanInfo()
            {
                return info;
            }

        }

        class ConfigurableDataSource
                extends EditableConfiguration
        {

            private ServiceReference<ServiceImporter<DataSource>> service;

            ConfigurableDataSource( ServiceReference<ServiceImporter<DataSource>> service, MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
            {
                super( info, identity, propertyNames );
                this.service = service;
            }

            @Override
            public Object invoke( String s, Object[] objects, String[] strings )
                    throws MBeanException, ReflectionException
            {
                if ( s.equals( "restart" ) ) {
                    try {
                        // Refresh and restart
                        if ( service.isActive() ) {
                            ( ( Activation ) service ).passivate();
                            ( ( Activation ) service ).activate();
                        }

                        return "Restarted DataSource";
                    } catch ( Exception e ) {
                        return "Could not restart DataSource:" + e.getMessage();
                    }
                }

                return "Unknown operation";
            }

        }

    }

}
