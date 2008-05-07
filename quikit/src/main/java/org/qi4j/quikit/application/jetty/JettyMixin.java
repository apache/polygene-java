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
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.This;
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
                       @This Configuration<HttpConfiguration> config )
    {
        this.config = config;
        int port = config.configuration().hostPort().get();
        server = new Server( port );
        String contextRoot = config.configuration().rootContextPath().get();
        if( contextRoot == null )
        {
            contextRoot = "/";
            config.configuration().rootContextPath().set( contextRoot );
        }
        Context root = new Context( server, contextRoot, Context.SESSIONS );
        File base = new File( getClass().getProtectionDomain().getCodeSource().getLocation().getPath() );
        root.setResourceBase( base.getAbsolutePath() );
        root.addServlet( DefaultServlet.class, "/" );
        for( ServiceReference<Servlet> servlet : servlets )
        {
            String path = servlet.getServiceAttribute( ServletInfo.class ).getPath();
            Servlet servletInstance = servlet.get();
            ServletHolder holder = new ServletHolder( servletInstance );
            root.addServlet( holder, path );
        }
    }

    public void activate() throws Exception
    {
        config.refresh();
        server.getConnectors()[ 0 ].setPort( config.configuration().hostPort().get() );
        server.start();
    }

    public void passivate() throws Exception
    {
        server.stop();
    }
}