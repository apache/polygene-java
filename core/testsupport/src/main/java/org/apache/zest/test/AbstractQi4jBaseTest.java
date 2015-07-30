/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.test;

import org.junit.After;
import org.junit.Before;
import org.apache.zest.api.Qi4j;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.spi.Qi4jSPI;

public abstract class AbstractQi4jBaseTest
{
    protected Qi4j api;
    protected Qi4jSPI spi;

    protected Energy4Java qi4j;
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    @Before
    public void setUp()
        throws Exception
    {
        qi4j = new Energy4Java();
        applicationModel = newApplication();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = newApplicationInstance( applicationModel );
        initApplication( application );
        api = spi = qi4j.spi();
        application.activate();
    }

    /** Called by the superclass for the test to define the entire application, every layer, every module and all
     * the contents of each module.
     *
     * @param applicationAssembly the {@link org.apache.zest.bootstrap.ApplicationAssembly} to be populated.
     */
    protected abstract void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException;

    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        return applicationModel.newInstance( qi4j.api() );
    }

    protected ApplicationDescriptor newApplication()
        throws AssemblyException
    {
        ApplicationAssembler assembler = new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
                applicationAssembly.setMode( Application.Mode.test );
                defineApplication( applicationAssembly );
                return applicationAssembly;
            }
        };

        try
        {
            return qi4j.newApplicationModel( assembler );
        }
        catch( AssemblyException e )
        {
            assemblyException( e );
            return null;
        }
    }

    /**
     * This method is called when there was an AssemblyException in the creation of the Zest application model.
     * <p>
     * Override this method to catch valid failures to place into satisfiedBy suites.
     * </p>
     * @param exception the exception thrown.
     *
     * @throws org.apache.zest.bootstrap.AssemblyException The default implementation of this method will simply re-throw the exception.
     */
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        throw exception;
    }

    protected void initApplication( Application app )
        throws Exception
    {
    }

    @After
    public void tearDown()
        throws Exception
    {
        if( application != null )
        {
            application.passivate();
        }
    }
}
