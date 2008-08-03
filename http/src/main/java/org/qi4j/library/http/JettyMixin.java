/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
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

import static org.mortbay.jetty.servlet.Context.SESSIONS;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Uses;
import org.qi4j.library.http.Dispatchers.Dispatcher;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceReference;

import com.sun.xml.internal.ws.util.StringUtils;

/**
 * TODO
 */
class JettyMixin
    implements Activatable
{
    private Server server;
    private HttpConfiguration configuration;

    public JettyMixin(
        @Service Iterable<ServiceReference<Servlet>> servlets,
        @Service Iterable<ServiceReference<Filter>> filters,
        @Uses ServiceDescriptor descriptor )
    {
        configuration = descriptor.metaInfo( HttpConfiguration.class );
        if( configuration == null )
        {
            throw new IllegalArgumentException( "Jetty service requires HttpConfiguration." );
        }

        // Create a server given the host port
        int port = configuration.getHostPort();
        server = new Server( port );

        // Sets the context root
        String contextRoot = configuration.getRootContextPath();
        if( contextRoot == null )
        {
            contextRoot = "/";
            configuration.setRootContextPath( contextRoot );
        }

        Context root = new Context( server, contextRoot, SESSIONS );

        // Sets the resource
        root.setResourceBase( rootResourceBase( configuration ) );

        // Sets the default servlet for default context
        root.addServlet( DefaultServlet.class, "/" );

        addServlets( root, servlets );
        addFilters( root, filters );
    }

    private void addServlets( Context root, Iterable<ServiceReference<Servlet>> servlets )
    {
        // Iterate the available servlets and add it to the server
        for( ServiceReference<Servlet> servlet : servlets )
        {
            ServletInfo servletInfo = servlet.metaInfo( ServletInfo.class );
            String servletPath = servletInfo.getPath();

            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            holder.setInitParameters( servletInfo.initParams() );
            root.addServlet( holder, servletPath );
        }
    }

    private void addFilters( Context root, Iterable<ServiceReference<Filter>> filters )
    {
        // Iterate the available filters and add it to the server
        for( ServiceReference<Filter> filter : filters )
        {
            FilterInfo filterInfo = filter.metaInfo( FilterInfo.class );
            String filterPath = filterInfo.getPath();

            Filter filterInstance = filter.get();
            FilterHolder holder = new FilterHolder( filterInstance );
            holder.setInitParameters( filterInfo.initParameters() );
            root.addFilter( holder, filterPath, toInt( filterInfo.dispatchers() ) );
        }
    }

    private int toInt( Dispatchers dispatches )
    {
        int value = 0;
        for ( Dispatcher dispatcher : dispatches )
        {
            value |= FilterHolder.dispatch( dispatcher.name().toLowerCase() );
        }
        return value;
    }

    private String rootResourceBase( HttpConfiguration configuration )
    {
        if ( configuration.getResourcePath() == null )
        {
            ProtectionDomain domain = getClass().getProtectionDomain();
            CodeSource source = domain.getCodeSource();
            URL location = source.getLocation();
            String basePath = location.getPath();
            File base = new File( basePath );
            return base.getAbsolutePath();
        }
        else
            return configuration.getResourcePath();
    }

    public final void activate() throws Exception
    {
        Connector[] connectors = server.getConnectors();
        Connector connector = connectors[ 0 ];

        int hostPort = configuration.getHostPort();
        connector.setPort( hostPort );
        server.start();
    }

    public final void passivate() throws Exception
    {
        server.stop();
    }
}