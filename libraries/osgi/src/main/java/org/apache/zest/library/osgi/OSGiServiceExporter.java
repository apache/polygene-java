/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.library.osgi;

import java.util.ArrayList;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.apache.zest.api.activation.ActivatorAdapter;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.service.qualifier.HasMetaInfo;
import org.apache.zest.api.util.Classes;
import org.apache.zest.functional.Iterables;

import static org.apache.zest.api.util.Classes.interfacesOf;

/**
 * Export Zest services to an OSGi Bundle.
 */
@Mixins( OSGiServiceExporter.OSGiServiceExporterMixin.class )
@Activators( OSGiServiceExporter.Activator.class )
public interface OSGiServiceExporter
    extends ServiceComposite
{

    void registerServices()
        throws Exception;

    void unregisterServices()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<OSGiServiceExporter>>
    {

        @Override
        public void afterActivation( ServiceReference<OSGiServiceExporter> activated )
            throws Exception
        {
            activated.get().registerServices();
        }

        @Override
        public void beforePassivation( ServiceReference<OSGiServiceExporter> passivating )
            throws Exception
        {
            passivating.get().unregisterServices();
        }

    }

    public static abstract class OSGiServiceExporterMixin
        implements OSGiServiceExporter
    {

        @Service
        @HasMetaInfo( BundleContext.class )
        private Iterable<ServiceReference<ServiceComposite>> services;
        private ArrayList<ServiceRegistration> registrations = new ArrayList<ServiceRegistration>();

        @Override
        public void registerServices()
            throws Exception
        {
            for( ServiceReference<ServiceComposite> ref : services )
            {
                Class<? extends BundleContext> type = BundleContext.class;
                BundleContext context = ref.metaInfo( type );
                ServiceComposite service = ref.get();
                Iterable<Class<?>> interfaces = Iterables.map( Classes.RAW_CLASS, interfacesOf( service.getClass() ) );
                String[] interfaceNames = new String[ (int) Iterables.count( interfaces ) ];
                Properties properties = ref.metaInfo( Properties.class );
                if( properties == null )
                {
                    properties = new Properties();
                }
                properties.put( "org.apache.zest.api.service.active", ref.isActive() );
                properties.put( "org.apache.zest.api.service.available", ref.isAvailable() );
                properties.put( "org.apache.zest.api.service.identity", ref.identity() );
                int i = 0;
                for( Class cls : interfaces )
                {
                    interfaceNames[ i++] = cls.getName();
                }
                registrations.add( context.registerService( interfaceNames, service, properties ) );

            }
        }

        @Override
        public void unregisterServices()
            throws Exception
        {
            for( ServiceRegistration reg : registrations )
            {
                reg.unregister();
            }
        }

    }

}
