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
package org.qi4j.library.struts2;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.ContainerProvider;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import javax.servlet.FilterConfig;
import org.apache.struts2.config.BeanSelectionProvider;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.FilterDispatcher;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.library.struts2.Constants.SERVLET_ATTRIBUTE;

public class Qi4jFilterDispatcher
    extends FilterDispatcher
{
    @Override
    protected Dispatcher createDispatcher( final FilterConfig filterConfig )
    {
        Dispatcher dispatcher = super.createDispatcher( filterConfig );
        ConfigurationManager configurationManager = createConfigurationManager( filterConfig );
        dispatcher.setConfigurationManager( configurationManager );
        return dispatcher;
    }

    protected ConfigurationManager createConfigurationManager( FilterConfig filterConfig )
    {
        ConfigurationManager configurationManager = new ConfigurationManager( BeanSelectionProvider.DEFAULT_BEAN_NAME );
        configurationManager.addContainerProvider( new Qi4jContainerProvider( module( filterConfig ) ) );
        return configurationManager;
    }

    private Module module( FilterConfig filterConfig )
    {
        return (Module) filterConfig.getServletContext().getAttribute( SERVLET_ATTRIBUTE );
    }

    class Qi4jContainerProvider
        implements ContainerProvider
    {
        private final Module module;
        private boolean registered = false;

        Qi4jContainerProvider( Module aModule )
        {
            module = aModule;
        }

        @Override
        public void register( ContainerBuilder builder, LocatableProperties props )
            throws ConfigurationException
        {
            factory( builder, UnitOfWorkFactory.class, module );
            factory( builder, ObjectFactory.class, module );
            factory( builder, TransientBuilderFactory.class, module );
            factory( builder, ActionConfiguration.class, actionConfiguration() );
            registered = true;
        }

        @Override
        public boolean needsReload()
        {
            return !registered;
        }

        @Override
        public void init( Configuration configuration )
            throws ConfigurationException
        {
        }

        @Override
        public void destroy()
        {
        }

        private <T> void factory( ContainerBuilder builder, Class<T> type, final T value )
        {
            builder.factory( type, new Factory<T>()
            {
                @Override
                public T create( Context context )
                {
                    return value;
                }
            } );
        }

        private ActionConfiguration actionConfiguration()
        {
            return module.findService( ActionService.class ).metaInfo( ActionConfiguration.class );
        }
    }
}
