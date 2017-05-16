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
package org.apache.polygene.library.servlet.lifecycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.common.InvalidApplicationException;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.library.servlet.PolygeneServlet;
import org.apache.polygene.library.servlet.PolygeneServletSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract ServletContextListener implementing ApplicationAssembler.
 *
 * Extends this class to easily bind a Polygene Application activation/passivation to your webapp lifecycle.
 *
 * The {@link Application} is set as a {@link ServletContext} attribute named using a constant.
 * In your servlets, filters, whatever has access to the {@link ServletContext} use the following code to get a
 * handle on the {@link Application}:
 *
 * <pre>
 *  org.apache.polygene.api.structure.Application application;
 *
 *  application = ( Application ) servletContext.getAttribute( PolygeneServletSupport.APP_IN_CTX );
 *
 *  // Or, shorter:
 *
 *  application = PolygeneServletSupport.application( servletContext );
 *
 * </pre>
 *
 * Rembember that the servlet specification states:
 *
 * In cases where the container is distributed over many virtual machines, a Web application will have an instance of
 * the ServletContext for each JVM.
 *
 * Context attributes are local to the JVM in which they were created. This prevents ServletContext attributes from
 * being a shared memory store in a distributed container. When information needs to be shared between servlets running
 * in a distributed environment, the information should be placed into a session, stored in a database, or set in an
 * Enterprise JavaBeans component.
 */
public abstract class AbstractPolygeneServletBootstrap
        implements ServletContextListener, ApplicationAssembler
{

    private static final Logger LOGGER = LoggerFactory.getLogger( PolygeneServlet.class.getPackage().getName() );
    // Polygene Runtime
    protected PolygeneAPI api;
    protected Energy4Java polygene;
    // Polygene Application
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    @Override
    public final void contextInitialized( ServletContextEvent sce )
    {
        try {

            ServletContext context = sce.getServletContext();

            LOGGER.trace( "Assembling Application" );
            polygene = new Energy4Java();
            applicationModel = polygene.newApplicationModel( this );

            LOGGER.trace( "Instanciating and activating Application" );
            application = applicationModel.newInstance( polygene.api() );
            api = polygene.api();
            beforeApplicationActivation( application );
            application.activate();
            afterApplicationActivation( application );

            LOGGER.trace( "Storing Application in ServletContext" );
            context.setAttribute( PolygeneServletSupport.APP_IN_CTX, application );

        } catch ( Exception ex ) {
            if ( application != null ) {
                try {
                    beforeApplicationPassivation( application );
                    application.passivate();
                    afterApplicationPassivation( application );
                } catch ( Exception ex1 ) {
                    LOGGER.warn( "Application not null and could not passivate it.", ex1 );
                }
            }
            throw new InvalidApplicationException( "Unexpected error during ServletContext initialization, see previous log for errors.", ex );
        }
    }

    protected void beforeApplicationActivation( Application app )
    {
    }

    protected void afterApplicationActivation( Application app )
    {
    }

    @Override
    public final void contextDestroyed( ServletContextEvent sce )
    {
        try {
            if ( application != null ) {
                beforeApplicationPassivation( application );
                application.passivate();
                afterApplicationPassivation( application );
            }
        } catch ( Exception ex ) {
            LOGGER.warn( "Unable to passivate Polygene Application.", ex );
        }
    }

    protected void beforeApplicationPassivation( Application app )
    {
    }

    protected void afterApplicationPassivation( Application app )
    {
    }

}
