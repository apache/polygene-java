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

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.management.MBeanServer;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Iterables;
import org.qi4j.library.http.Interface.Protocol;

import static org.qi4j.library.http.JettyConfigurationHelper.addContextListeners;
import static org.qi4j.library.http.JettyConfigurationHelper.addFilters;
import static org.qi4j.library.http.JettyConfigurationHelper.addServlets;
import static org.qi4j.library.http.JettyConfigurationHelper.configureConnector;
import static org.qi4j.library.http.JettyConfigurationHelper.configureContext;
import static org.qi4j.library.http.JettyConfigurationHelper.configureHttp;
import static org.qi4j.library.http.JettyConfigurationHelper.configureServer;

public abstract class AbstractJettyMixin
    implements HttpService, JettyActivation
{

    private final String identity;

    private final Iterable<ServiceReference<ServletContextListener>> contextListeners;

    private final Iterable<ServiceReference<Servlet>> servlets;

    private final Iterable<ServiceReference<Filter>> filters;

    private final MBeanServer mBeanServer;

    private Server server;

    public AbstractJettyMixin( String identity, Server jettyServer,
                               Iterable<ServiceReference<ServletContextListener>> contextListeners,
                               Iterable<ServiceReference<Servlet>> servlets,
                               Iterable<ServiceReference<Filter>> filters,
                               MBeanServer mBeanServer )
    {
        this.identity = identity;
        this.server = jettyServer;
        this.contextListeners = Iterables.unique( contextListeners );
        this.servlets = Iterables.unique( servlets );
        this.filters = Iterables.unique( filters );
        this.mBeanServer = mBeanServer;
    }

    @Override
    public final void startJetty()
        throws Exception
    {
        // Configure Server
        configureServer( server, configuration() );

        // Set up HTTP
        HttpConfiguration httpConfig = new HttpConfiguration();
        configureHttp( httpConfig, configuration() );
        httpConfig = specializeHttp( httpConfig );

        // Set up connector
        ServerConnector connector = buildConnector( server, httpConfig );
        configureConnector( connector, configuration() );

        // Bind Connector to Server
        server.addConnector( connector );
        if( mBeanServer != null )
        {
            server.addEventListener( new MBeanContainer( mBeanServer ) );
        }

        // Prepare ServletContext
        ServletContextHandler root = new ServletContextHandler( server,
                                                                "/",
                                                                new SessionHandler(),
                                                                buildSecurityHandler(),
                                                                new ServletHandler(),
                                                                new ErrorHandler() );
        root.setDisplayName( identity );
        configureContext( root, configuration() );

        // Register ContextListeners, Servlets and Filters
        addContextListeners( root, contextListeners );
        addServlets( root, servlets );
        addFilters( root, filters );

        // Start
        server.start();
    }

    @Override
    public final void stopJetty()
        throws Exception
    {
        server.stop();
        for( Connector connector : server.getConnectors() )
        {
            connector.stop();
        }
        server = null;
    }

    @Override
    @SuppressWarnings( "ValueOfIncrementOrDecrementUsed" )
    public final Interface[] interfacesServed()
    {
        Connector[] connectors = server.getConnectors();
        Interface[] result = new Interface[ connectors.length ];
        int index = 0;
        for( Connector connector : connectors )
        {
            if( connector instanceof NetworkConnector )
            {
                NetworkConnector netConnector = (NetworkConnector) connector;
                String host = configuration().hostName().get();
                if( host == null )
                {
                    host = netConnector.getHost();
                    if( host == null ) // If serving all interfaces.
                    {
                        try
                        {
                            host = InetAddress.getLocalHost().getHostAddress();
                        }
                        catch( UnknownHostException e )
                        {
                            InternalError error = new InternalError( "UnknownHost for local interface." );
                            error.initCause( e );
                            throw error;
                        }
                    }
                }
                result[ index++] = new InterfaceImpl( host, netConnector.getPort(), servedProtocol() );
            }
        }
        return result;
    }

    protected abstract JettyConfiguration configuration();

    @SuppressWarnings( "NoopMethodInAbstractClass" )
    protected HttpConfiguration specializeHttp( HttpConfiguration httpConfig )
    {
        return httpConfig;
    }

    protected ServerConnector buildConnector( Server server, HttpConfiguration httpConfig )
    {
        return new ServerConnector( server, new HttpConnectionFactory( httpConfig ) );
    }

    protected SecurityHandler buildSecurityHandler()
    {
        return null;
    }

    protected abstract Protocol servedProtocol();

}
