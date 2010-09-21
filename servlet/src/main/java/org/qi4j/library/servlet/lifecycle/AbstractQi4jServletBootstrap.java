/*
 * Copyright (c) 2009 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.servlet.lifecycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.qi4j.api.Qi4j;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
//import org.qi4j.envisage.model.descriptor.ApplicationDetailDescriptor;
//import org.qi4j.envisage.model.descriptor.ApplicationDetailDescriptorBuilder;
//import org.qi4j.envisage.model.descriptor.LayerDetailDescriptor;
//import org.qi4j.envisage.model.descriptor.ModuleDetailDescriptor;
import org.qi4j.library.servlet.Qi4jServlet;
import org.qi4j.library.servlet.Qi4jServletSupport;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.ApplicationSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We set the Qi4j Application as attribute on the ServletContext so they it is bound to the webapp lifecycle.
 *
 * Servlet specification states:
 *
 *      In cases where the container is distributed over many virtual machines, a Web application will have an
 *      instance of the ServletContext for each JVM.
 *
 *      Context attributes are local to the JVM in which they were created. This prevents ServletContext attributes
 *      from being a shared memory store in a distributed container. When information needs to be shared between
 *      servlets running in a distributed environment, the information should be placed into a session, stored in a
 *      database, or set in an Enterprise JavaBeans component.
 *
 * @author Paul Merlin <paul@nosphere.org>
 */
public abstract class AbstractQi4jServletBootstrap
        implements ServletContextListener, ApplicationAssembler
{

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractQi4jServletBootstrap.class );
    // Qi4j Runtime
    protected Qi4j api;
    protected Qi4jSPI spi;
    protected Energy4Java qi4j;
    // Qi4j Application
    protected ApplicationModelSPI applicationModel;
    protected ApplicationSPI application;
//    protected ApplicationDetailDescriptor descriptor;

    @Override
    public final void contextInitialized( ServletContextEvent sce )
    {
        try {

            ServletContext context = sce.getServletContext();

            LOGGER.debug( "Assembling Application" );
            qi4j = new Energy4Java();
            applicationModel = qi4j.newApplicationModel( this );
//            descriptor = ApplicationDetailDescriptorBuilder.createApplicationDetailDescriptor( applicationModel );

            LOGGER.debug( "Instanciating and activating Application" );
            application = applicationModel.newInstance( qi4j.spi() );
            spi = qi4j.spi();
            api = spi;
            beforeApplicationActivation( application );
            application.activate();
            afterApplicationActivation( application );

            LOGGER.debug( "Storing Application in ServletContext" );
            context.setAttribute( Qi4jServletSupport.APP_IN_CTX, application );

        } catch ( Exception ex ) {
            if ( application != null ) {
                try {
                    application.passivate();
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
//                for ( LayerDetailDescriptor eachLayer : descriptor.layers() ) {
//                    for ( ModuleDetailDescriptor eachModule : eachLayer.modules() ) {
//                        String layerName = eachLayer.descriptor().name();
//                        String moduleName = eachModule.descriptor().name();
//                        LOGGER.debug( "ContextDestroyed UOWF check in: Application > " + layerName + " > " + moduleName );
//                        Module module = application.findModule( layerName, moduleName );
//                        UnitOfWorkFactory eachUowf = module.unitOfWorkFactory();
//                        if ( eachUowf != null && eachUowf.currentUnitOfWork() != null ) {
//                            UnitOfWork current;
//                            while ( ( current = eachUowf.currentUnitOfWork() ) != null ) {
//                                if ( current.isOpen() ) {
//                                    current.discard();
//                                } else {
//                                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened." );
//                                }
//                            }
//                            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
//                        }
//                    }
//                }
                application.passivate();
            }
        } catch ( Exception ex ) {
            LOGGER.warn( "Unable to passivate Qi4j Application.", ex );
        }
    }

}
