/*
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

import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.http.Interface.Protocol;
import static org.qi4j.library.http.JettyConfigurationHelper.*;

public class SecureJettyMixin
        extends AbstractJettyMixin
{

    @This
    private Configuration<SecureJettyConfiguration> configuration;
    @Optional
    @Service
    private Iterable<ConstraintService> constraintServices;

    public SecureJettyMixin( @This Identity meAsIdentity,
                             @Service Server jettyServer,
                             @Service Iterable<ServiceReference<Servlet>> servlets,
                             @Service Iterable<ServiceReference<Filter>> filters,
                             @Optional @Service MBeanServer mBeanServer )
    {
        super( meAsIdentity.identity().get(), jettyServer, servlets, filters, mBeanServer );
    }

    @Override
    protected JettyConfiguration configuration()
    {
        return configuration.configuration();
    }

    @Override
    protected SecurityHandler buildSecurityHandler()
    {
        if ( constraintServices != null ) {
            ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
            for ( ConstraintService eachConstraintService : constraintServices ) {
                ConstraintMapping csMapping = eachConstraintService.buildConstraintMapping();
                if ( csMapping != null ) {
                    securityHandler.addConstraintMapping( csMapping );
                }
            }
            if ( !securityHandler.getConstraintMappings().isEmpty() ) {
                return securityHandler;
            }
        }
        return super.buildSecurityHandler();
    }

    @Override
    protected Connector buildConnector()
    {
        SslConnector connector = new SslSelectChannelConnector( new SslContextFactory() );
        configureSslConnector( connector, configuration.configuration() );
        return connector;
    }

    @Override
    protected Protocol servedProtocol()
    {
        return Protocol.https;
    }

}