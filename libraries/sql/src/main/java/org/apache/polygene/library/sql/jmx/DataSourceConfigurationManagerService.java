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
package org.apache.polygene.library.sql.jmx;

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
import org.apache.polygene.api.activation.Activation;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.library.sql.datasource.DataSourceConfiguration;
import org.apache.polygene.spi.PolygeneSPI;

/**
 * Expose DataSourceConfiguration through JMX.
 * Allow configurations to be edited, and the services to be restarted.
 */
@Mixins( DataSourceConfigurationManagerService.Mixin.class )
@Activators( DataSourceConfigurationManagerService.Activator.class )
public interface DataSourceConfigurationManagerService
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
        UnitOfWorkFactory uowf;

        @Service
        MBeanServer server;

        @Structure
        PolygeneSPI spi;

        @Structure
        Application application;

        @Service
        Iterable<ServiceReference<DataSource>> dataSources;

        @Service
        ServiceReference<ServiceImporter<DataSource>> dataSourceService;

        private List<ObjectName> configurationNames = new ArrayList<>();

        @Override
        public void exportDataSources()
                throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException
        {
            for ( ServiceReference<DataSource> dataSource : dataSources ) {
                ModuleDescriptor module = spi.moduleOf( dataSource );
                EntityDescriptor descriptor = module.entityDescriptor( DataSourceConfiguration.class.getName() );
                List<MBeanAttributeInfo> attributes = new ArrayList<>();
                Map<String, AccessibleObject> properties = new LinkedHashMap<>();
                descriptor.state().properties().forEach(persistentProperty -> {
                    if ( !persistentProperty.isImmutable() ) {
                        String propertyName = persistentProperty.qualifiedName().name();
                        String type = persistentProperty.valueType().primaryType().getName();
                        attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ) ) );
                        properties.put( propertyName, persistentProperty.accessor() );
                    }
                } );

                List<MBeanOperationInfo> operations = new ArrayList<>();
                operations.add( new MBeanOperationInfo( "restart", "Restart DataSource", new MBeanParameterInfo[ 0 ], "void", MBeanOperationInfo.ACTION_INFO ) );

                String mbeanName = dataSource.identity().toString();
                MBeanInfo mbeanInfo = new MBeanInfo( DataSourceConfiguration.class.getName(), mbeanName, attributes.toArray( new MBeanAttributeInfo[ attributes.size() ] ), null, operations.toArray( new MBeanOperationInfo[ operations.size() ] ), null );
                Object mbean = new ConfigurableDataSource( dataSourceService, mbeanInfo, mbeanName, properties );
                ObjectName configurableDataSourceName = new ObjectName( "Polygene:application=" + application.name() + ",class=Datasource,name=" + mbeanName );
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

            Identity identity;

            Map<String, AccessibleObject> propertyNames;

            EditableConfiguration( MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
            {
                this.info = info;
                this.identity = StringIdentity.fromString( identity );
                this.propertyNames = propertyNames;
            }

            @Override
            public Object getAttribute( String name )
                    throws AttributeNotFoundException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
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
                UnitOfWork uow = uowf.newUnitOfWork();
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
                    } catch ( AttributeNotFoundException | MBeanException | ReflectionException e ) {
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
                    } catch ( AttributeNotFoundException | InvalidAttributeValueException | ReflectionException | MBeanException e ) {
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
