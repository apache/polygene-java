/*
 * Copyright 2013 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.activation;

import java.io.PrintStream;
import org.qi4j.api.structure.Application;
import org.slf4j.Logger;

/**
 * Application Passivation Thread to use as a Shutdown Hook.
 * <pre>Runtime.getRuntime().addShutdownHook( new ApplicationPassivationThread( application ) );</pre>
 * <p>Constructors to control where errors are logged are provided. They support PrintStream (STDOUT/STDERR) and SLF4J
 * Loggers. Defaults to STDERR.</p>
 */
public final class ApplicationPassivationThread
    extends Thread
{
    /**
     * Create a new Application Passivation Thread that output errors to STDERR.
     * @param application The Application to passivate
     */
    public ApplicationPassivationThread( final Application application )
    {
        this( application, null, null );
    }

    /**
     * Create a new Application Passivation Thread that output errors to a Logger.
     * @param application The Application to passivate
     * @param logger Logger for errors
     */
    public ApplicationPassivationThread( Application application, Logger logger )
    {
        this( application, null, logger );
    }

    /**
     * Create a new Application Passivation Thread that output errors to a PrintStream.
     * @param application The Application to passivate
     * @param output PrintStream for errors
     */
    public ApplicationPassivationThread( Application application, PrintStream output )
    {
        this( application, output, null );
    }

    private ApplicationPassivationThread( Application application, PrintStream output, Logger logger )
    {
        super( new ApplicationPassivation( application, output, logger ),
               application.name() + " Passivation Thread" );
    }

    private static class ApplicationPassivation
        implements Runnable
    {

        private final Application application;
        private final PrintStream output;
        private final Logger logger;

        private ApplicationPassivation( Application application, PrintStream output, Logger logger )
        {
            this.application = application;
            this.output = output;
            this.logger = logger;
        }

        @Override
        public void run()
        {
            try
            {
                application.passivate();
            }
            catch( PassivationException ex )
            {
                String message = application.name() + " " + ex.getMessage();
                if( logger != null )
                {
                    logger.error( message, ex );
                }
                else if( output != null )
                {
                    output.println( message );
                    ex.printStackTrace( output );
                }
                else
                {
                    System.err.println( message );
                    ex.printStackTrace( System.err );
                }
            }
        }

    }

}
