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
package org.apache.polygene.library.http;

import javax.management.MBeanServer;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.apache.polygene.api.identity.HasIdentity;
import org.eclipse.jetty.server.Server;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.library.http.Interface.Protocol;

public class JettyMixin
        extends AbstractJettyMixin
{

    @This
    private Configuration<JettyConfiguration> configuration;

    public JettyMixin( @This HasIdentity meAsIdentity,
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