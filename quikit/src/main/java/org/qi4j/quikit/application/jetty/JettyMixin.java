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

package org.qi4j.quikit.application.jetty;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import javax.servlet.Servlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import static org.mortbay.jetty.servlet.Context.SESSIONS;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Uses;
import org.qi4j.quikit.application.ServletInfo;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceReference;

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
        root.setResourceBase( rootResourceBase() );

        // Sets the default servlet for default context
        root.addServlet( DefaultServlet.class, "/" );

        // Iterate the available servlets and add it to the server
        for( ServiceReference<Servlet> servlet : servlets )
        {
            ServletInfo servletInfo = servlet.metaInfo( ServletInfo.class );
            String servletPath = servletInfo.getPath();

            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            root.addServlet( holder, servletPath );
        }
    }

    private String rootResourceBase()
    {
        ProtectionDomain domain = getClass().getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URL location = source.getLocation();
        String basePath = location.getPath();
        File base = new File( basePath );
        return base.getAbsolutePath();
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