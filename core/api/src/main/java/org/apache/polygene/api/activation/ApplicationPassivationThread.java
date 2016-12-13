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
package org.apache.polygene.api.activation;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.polygene.api.structure.Application;

/**
 * Application Passivation Thread to use as a Shutdown Hook.
 * <pre>Runtime.getRuntime().addShutdownHook( new ApplicationPassivationThread( application ) );</pre>
 */
public final class ApplicationPassivationThread
    extends Thread
{
    /**
     * Create a new Application Passivation Thread that output errors to STDERR.
     * @param application The Application to passivate
     */
    @SuppressWarnings("unused")
    public ApplicationPassivationThread(final Application application )
    {
        this( application, null, null );
    }

    /**
     * Create a new Application Passivation Thread that output errors to a Logger.
     * @param application The Application to passivate
     * @param logger Logger for errors
     */
    @SuppressWarnings("unused")
    public ApplicationPassivationThread(Application application, Logger logger )
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
                    logger.log( Level.SEVERE, message, ex );
                }
                else if( output != null )
                {
                    output.println( message );
                    ex.printStackTrace( output );
                }
                else
                {
                    ex.printStackTrace();
                }
            }
        }

    }

}
