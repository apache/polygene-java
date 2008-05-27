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
import javax.servlet.Servlet;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import static org.mortbay.jetty.servlet.Context.*;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.This;
import org.qi4j.property.Property;
import org.qi4j.quikit.application.ServletInfo;
import org.qi4j.quikit.assembly.composites.HttpConfiguration;
import org.qi4j.service.Activatable;
import org.qi4j.service.Configuration;
import org.qi4j.service.ServiceReference;

/**
 * TODO
 */
public class JettyMixin
    implements Activatable
{
    private Server server;
    private Configuration<HttpConfiguration> config;

    public JettyMixin( @Service Iterable<ServiceReference<Servlet>> servlets,
                       @This Configuration<HttpConfiguration> aConfig )
    {
        config = aConfig;

        // Create a server given the host port
        HttpConfiguration configuration = aConfig.configuration();
        int port = configuration.hostPort().get();
        server = new Server( port );

        // Sets the context root
        Property<String> rootContextPathProperty = configuration.rootContextPath();
        String contextRoot = rootContextPathProperty.get();
        if( contextRoot == null )
        {
            contextRoot = "/";
            rootContextPathProperty.set( contextRoot );
        }

        Context root = new Context( server, contextRoot, SESSIONS );
        File base = new File( getClass().getProtectionDomain().getCodeSource().getLocation().getPath() );

        // Sets the resource
        root.setResourceBase( base.getAbsolutePath() );

        // Sets the default servlet for default context
        root.addServlet( DefaultServlet.class, "/" );

        // Iterate the available servlets and add it to the server
        for( ServiceReference<Servlet> servlet : servlets )
        {
            ServletInfo servletInfo = servlet.getServiceAttribute( ServletInfo.class );
            String path = servletInfo.getPath();
            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            root.addServlet( holder, path );
        }
    }

    public void activate() throws Exception
    {
        config.refresh();

        Connector[] connectors = server.getConnectors();
        Connector connector = connectors[ 0 ];

        HttpConfiguration configuration = config.configuration();
        Integer hostPort = configuration.hostPort().get();
        connector.setPort( hostPort );
        server.start();
    }

    public void passivate() throws Exception
    {
        server.stop();
    }
}