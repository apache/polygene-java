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
package org.apache.polygene.test;

import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.spi.PolygeneSPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractPolygeneBaseTest
{
    protected PolygeneAPI api;
    protected PolygeneSPI spi;

    protected Energy4Java polygene;
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        polygene = new Energy4Java();
        applicationModel = newApplicationModel();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = newApplicationInstance( applicationModel );
        initApplication( application );
        api = spi = polygene.spi();
        application.activate();
    }

    /** Called by the superclass for the test to define the entire application, every layer, every module and all
     * the contents of each module.
     *
     * @param applicationAssembly the {@link org.apache.polygene.bootstrap.ApplicationAssembly} to be populated.
     *
     * @throws AssemblyException on invalid assembly
     */
    protected abstract void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException;

    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        return applicationModel.newInstance( polygene.api() );
    }

    protected ApplicationDescriptor newApplicationModel()
        throws AssemblyException
    {
        ApplicationAssembler assembler = applicationFactory ->
        {
            ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
            applicationAssembly.setMode( Application.Mode.test );
            defineApplication( applicationAssembly );
            return applicationAssembly;
        };

        try
        {
            return polygene.newApplicationModel( assembler );
        }
        catch( AssemblyException e )
        {
            assemblyException( e );
            return null;
        }
    }

    /**
     * This method is called when there was an AssemblyException in the creation of the Polygene application model.
     * <p>
     * Override this method to catch valid failures to place into satisfiedBy suites.
     * </p>
     * @param exception the exception thrown.
     *
     * @throws org.apache.polygene.bootstrap.AssemblyException The default implementation of this method will simply re-throw the exception.
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

    @AfterEach
    public void tearDown()
    {
        if( application != null )
        {
            try
            {
                application.passivate();
            } catch( Exception e )
            {
                throw new RuntimeException( "Unable to shut down test harness cleanly.", e );
            }
        }
    }
}
