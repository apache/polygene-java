/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.spi.structure.DescriptorVisitor;

/**
 * JAVADOC
 */
@Mixins( JMXConfigurationService.JMXConfigurationMixin.class )
public interface JMXConfigurationService
    extends ServiceComposite, Activatable
{
    class JMXConfigurationMixin
        implements Activatable
    {
        @Service
        MBeanServer mbeanServer;
        @Structure
        ApplicationSPI app;
        @Structure
        UnitOfWorkFactory uowf;
        @Structure
        Qi4jSPI spi;

        public void activate()
            throws Exception
        {
            final UnitOfWork uow = uowf.newUnitOfWork();

            app.visitDescriptor( new DescriptorVisitor()
            {
                @Override
                public void visit( ServiceDescriptor serviceDescriptor )
                {
                    String identity = serviceDescriptor.identity();
                    Class config = serviceDescriptor.configurationType();
                    if( config != null )
                    {
                        // Register MBean for Service Configuration
                        try
                        {
                            ConfigurationComposite configuration = (ConfigurationComposite) uow.get( config, identity );
                            MBeanInfo mbeanInfo = new MBeanInfo( serviceDescriptor.type().toString(), "", attributes( configuration ), new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0] );
                            mbeanServer.registerMBean( new EntityMBean( configuration, mbeanInfo ), new ObjectName( "Configuration:service=" + configuration
                                .identity() ) );
                        }
                        catch( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            } );

            uow.pause();
        }

        public void passivate()
            throws Exception
        {
        }

        private MBeanAttributeInfo[] attributes( ConfigurationComposite configuration )
        {
            EntityDescriptor descriptor = spi.getEntityDescriptor( configuration );
            List<MBeanAttributeInfo> infoList = new ArrayList<MBeanAttributeInfo>();
            for( PropertyDescriptor propertyType : descriptor.state().properties() )
            {
                MBeanAttributeInfo info = new MBeanAttributeInfo( propertyType.qualifiedName().name(), propertyType.type().toString(), null, true, !propertyType
                    .isImmutable(), false );
                infoList.add( info );
            }

            return infoList.toArray( new MBeanAttributeInfo[infoList.size()] );
        }
    }
}
