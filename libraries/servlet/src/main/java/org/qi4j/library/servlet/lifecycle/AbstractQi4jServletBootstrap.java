/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.servlet.lifecycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.qi4j.api.Qi4j;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.library.servlet.Qi4jServlet;
import org.qi4j.library.servlet.Qi4jServletSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract ServletContextListener implementing ApplicationAssembler.
 *
 * Extends this class to easily bind a Qi4j Application activation/passivation to your webapp lifecycle.
 *
 * The {@link Application} is set as a {@link ServletContext} attribute named using a constant.
 * In your servlets, filters, whatever has access to the {@link ServletContext} use the following code to get a
 * handle on the {@link Application}:
 *
 * <pre>
 *  org.qi4j.api.structure.Application application;
 *
 *  application = ( Application ) servletContext.getAttribute( Qi4jServletSupport.APP_IN_CTX );
 *
 *  // Or, shorter:
 *
 *  application = Qi4jServletSupport.application( servletContext );
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
public abstract class AbstractQi4jServletBootstrap
        implements ServletContextListener, ApplicationAssembler
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Qi4jServlet.class.getPackage().getName() );
    // Qi4j Runtime
    protected Qi4j api;
    protected Energy4Java qi4j;
    // Qi4j Application
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    @Override
    public final void contextInitialized( ServletContextEvent sce )
    {
        try {

            ServletContext context = sce.getServletContext();

            LOGGER.trace( "Assembling Application" );
            qi4j = new Energy4Java();
            applicationModel = qi4j.newApplicationModel( this );

            LOGGER.trace( "Instanciating and activating Application" );
            application = applicationModel.newInstance( qi4j.api() );
            api = qi4j.api();
            beforeApplicationActivation( application );
            application.activate();
            afterApplicationActivation( application );

            LOGGER.trace( "Storing Application in ServletContext" );
            context.setAttribute( Qi4jServletSupport.APP_IN_CTX, application );

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
            LOGGER.warn( "Unable to passivate Qi4j Application.", ex );
        }
    }

    protected void beforeApplicationPassivation( Application app )
    {
    }

    protected void afterApplicationPassivation( Application app )
    {
    }

}
