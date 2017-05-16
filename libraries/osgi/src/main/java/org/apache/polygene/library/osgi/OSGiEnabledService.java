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
package org.apache.polygene.library.osgi;

import java.lang.reflect.Type;
import java.util.Dictionary;
import java.util.stream.Stream;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.apache.polygene.api.util.Classes.toClassName;
import static org.apache.polygene.api.util.Classes.typesOf;

/**
 * Service Fragment providing OSGi support.
 */
@Mixins( OSGiEnabledService.OSGiEnabledServiceMixin.class )
@Activators( OSGiEnabledService.Activator.class )
public interface OSGiEnabledService extends HasIdentity
{
    void registerServices()
        throws Exception;

    void unregisterServices()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<OSGiEnabledService>>
    {

        @Override
        public void afterActivation( ServiceReference<OSGiEnabledService> activated )
            throws Exception
        {
            activated.get().registerServices();
        }

        @Override
        public void beforePassivation( ServiceReference<OSGiEnabledService> passivating )
            throws Exception
        {
            passivating.get().unregisterServices();
        }
    }

    abstract class OSGiEnabledServiceMixin
        implements OSGiEnabledService
    {
        @Uses
        ServiceDescriptor descriptor;

        @Structure
        private Module module;

        private ServiceRegistration registration;

        @Override
        public void registerServices()
            throws Exception
        {
            BundleContext context = descriptor.metaInfo( BundleContext.class );
            if( context == null )
            {
                return;
            }
            module.findServices( descriptor.types().findFirst().orElse( null ) ).forEach(
                ref -> {
                    if( ref.identity().equals( identity().get() ) )
                    {
                        Stream<? extends Type> classesSet = descriptor.types();
                        Dictionary properties = descriptor.metaInfo( Dictionary.class );
                        String[] clazzes = fetchInterfacesImplemented( classesSet );
                        registration = context.registerService( clazzes, ref.get(), properties );
                    }
                }
            );
        }

        private String[] fetchInterfacesImplemented( Stream<? extends Type> classesSet )
        {
            return typesOf( classesSet ).map( toClassName() ).toArray( String[]::new );
        }

        @Override
        public void unregisterServices()
            throws Exception
        {
            if( registration != null )
            {
                registration.unregister();
                registration = null;
            }
        }
    }
}
