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
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.library.http.Interface.Protocol;
import static org.qi4j.library.http.JettyConfigurationHelper.*;

public abstract class AbstractJettyMixin
        implements Activatable, HttpService
{

    private final String identity;
    private final Iterable<ServiceReference<Servlet>> servlets;
    private final Iterable<ServiceReference<Filter>> filters;
    private final MBeanServer mBeanServer;
    private Server server;

    public AbstractJettyMixin( String identity, Server jettyServer, Iterable<ServiceReference<Servlet>> servlets, Iterable<ServiceReference<Filter>> filters, MBeanServer mBeanServer )
    {
        this.identity = identity;
        this.server = jettyServer;
        this.servlets = servlets;
        this.filters = filters;
        this.mBeanServer = mBeanServer;
    }

    protected abstract JettyConfiguration configuration();

    protected Connector buildConnector()
    {
        return new SelectChannelConnector();
    }

    protected SecurityHandler buildSecurityHandler()
    {
        return null;
    }

    protected abstract Protocol servedProtocol();

    public final void activate()
            throws Exception
    {
        // server = new Server();

        // Prepare ServletContext
        ServletContextHandler root = new ServletContextHandler( server,
                                                                "/",
                                                                new SessionHandler(),
                                                                buildSecurityHandler(),
                                                                new ServletHandler(),
                                                                new ErrorHandler() );
        root.setDisplayName( identity );
        configureContext( root, configuration() );

        // Register Servlets and Filters
        addServlets( root, servlets );
        addFilters( root, filters );

        // Prepare Connector
        Connector connector = buildConnector();
        configureConnector( connector, configuration() );

        // Prepare Server
        configureServer( server, configuration() );
        server.addConnector( connector );
        if ( mBeanServer != null ) {
            server.getContainer().addEventListener( new MBeanContainer( mBeanServer ) );
        }

        // Start
        server.start();
    }

    public final void passivate()
            throws Exception
    {
        server.stop();
        for ( Connector connector : server.getConnectors() ) {
            connector.close();
        }
        server = null;
    }

    public final Interface[] interfacesServed()
    {
        Connector[] connectors = server.getConnectors();
        Interface[] result = new Interface[ connectors.length ];
        int index = 0;
        for ( Connector connector : connectors ) {
            String host = configuration().hostName().get();
            if ( host == null ) {
                host = connector.getHost();
                if ( host == null ) // If serving all interfaces.
                {
                    try {
                        host = InetAddress.getLocalHost().getHostAddress();
                    } catch ( UnknownHostException e ) {
                        InternalError error = new InternalError( "UnknownHost for local interface." );
                        error.initCause( e );
                        throw error;
                    }
                }
            }
            result[ index++] = new InterfaceImpl( host, connector.getPort(), servedProtocol() );

        }
        return result;
    }

    protected static void addServlets( ServletContextHandler root, Iterable<ServiceReference<Servlet>> servlets )
    {
        // Iterate the available servlets and add it to the server
        for ( ServiceReference<Servlet> servlet : servlets ) {
            ServletInfo servletInfo = servlet.metaInfo( ServletInfo.class );
            String servletPath = servletInfo.getPath();
            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            holder.setInitParameters( servletInfo.initParams() );
            root.addServlet( holder, servletPath );
        }
    }

    protected static void addFilters( ServletContextHandler root, Iterable<ServiceReference<Filter>> filters )
    {
        // Iterate the available filters and add it to the server
        for ( ServiceReference<Filter> filter : filters ) {
            FilterInfo filterInfo = filter.metaInfo( FilterInfo.class );
            String filterPath = filterInfo.getPath();

            Filter filterInstance = filter.get();
            FilterHolder holder = new FilterHolder( filterInstance );
            holder.setInitParameters( filterInfo.initParameters() );
            root.addFilter( holder, filterPath, filterInfo.dispatchers() );
        }
    }

}
