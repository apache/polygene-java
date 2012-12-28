/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import javax.management.MBeanServer;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.server.Server;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.http.Interface.Protocol;

public class JettyMixin
        extends AbstractJettyMixin
{

    @This
    private Configuration<JettyConfiguration> configuration;

    public JettyMixin( @This Identity meAsIdentity,
                       @Service Server jettyServer,
                       @Service Iterable<ServiceReference<ServletContextListener>> contextListeners,
                       @Service Iterable<ServiceReference<Servlet>> servlets,
                       @Service Iterable<ServiceReference<Filter>> filters,
                       @Optional @Service MBeanServer mBeanServer )
    {
        super( meAsIdentity.identity().get(), jettyServer, contextListeners, servlets, filters, mBeanServer );
    }

    @Override
    protected JettyConfiguration configuration()
    {
        return configuration.get();
    }

    @Override
    protected Protocol servedProtocol()
    {
        return Protocol.http;
    }

}