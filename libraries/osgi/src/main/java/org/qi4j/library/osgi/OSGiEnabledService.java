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
package org.qi4j.library.osgi;

import java.lang.reflect.Type;
import java.util.Dictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

import static org.qi4j.api.util.Classes.toClassName;
import static org.qi4j.api.util.Classes.typesOf;
import static org.qi4j.functional.Iterables.cast;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.toArray;

/**
 * Service Fragment providing OSGi support.
 */
@Mixins( OSGiEnabledService.OSGiEnabledServiceMixin.class )
@Activators( OSGiEnabledService.Activator.class )
public interface OSGiEnabledService extends ServiceComposite
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


    public abstract class OSGiEnabledServiceMixin
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
            for( ServiceReference ref : module.findServices( first( descriptor.types() ) ) )
            {
                if( ref.identity().equals( identity().get() ) )
                {
                    Iterable<Type> classesSet = cast(descriptor.types());
                    Dictionary properties = descriptor.metaInfo( Dictionary.class );
                    String[] clazzes = fetchInterfacesImplemented( classesSet );
                    registration = context.registerService( clazzes, ref.get(), properties );
                }
            }
        }

        private String[] fetchInterfacesImplemented( Iterable<Type> classesSet )
        {
            return toArray( String.class, map( toClassName(), typesOf( classesSet ) ) );
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
