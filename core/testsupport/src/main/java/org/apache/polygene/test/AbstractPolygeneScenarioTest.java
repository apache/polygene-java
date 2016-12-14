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
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.spi.PolygeneSPI;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for Polygene scenario tests. This will create one Polygene application per class instead of per test.
 */
public abstract class AbstractPolygeneScenarioTest
    implements Assembler
{
    static protected PolygeneAPI api;
    static protected PolygeneSPI spi;

    static protected Energy4Java polygene;
    static protected ApplicationDescriptor applicationModel;
    static protected Application application;

    static protected Module module;

    static protected Assembler assembler; // Initialize this in static block of subclass
    private static UnitOfWorkFactory uowf;

    @BeforeClass
    public static void setUp()
        throws Exception
    {
        polygene = new Energy4Java();
        applicationModel = newApplication();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = applicationModel.newInstance( polygene.spi() );
        initApplication( application );
        api = spi = polygene.spi();
        application.activate();

        // Assume only one module
        module = application.findModule( "Layer 1", "Module 1" );
        uowf = module.unitOfWorkFactory();
    }

    static protected ApplicationDescriptor newApplication()
        throws AssemblyException
    {
        final Assembler asm = assembler;

        ApplicationAssembler assembler = applicationFactory -> applicationFactory.newApplicationAssembly( asm );
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
     *
     * @param exception the exception thrown.
     *
     * @throws org.apache.polygene.bootstrap.AssemblyException The default implementation of this method will simply re-throw the exception.
     */
    static protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        throw exception;
    }

    static protected void initApplication( Application app )
        throws Exception
    {
    }

    @AfterClass
    public void tearDown()
        throws Exception
    {
        if( uowf != null && uowf.isUnitOfWorkActive() )
        {
            while( uowf.isUnitOfWorkActive() )
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                if( uow.isOpen() )
                {
                    uow.discard();
                }
                else
                {
                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened. First is" + uow
                        .usecase()
                        .name() );
                }
            }
            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }

        if( application != null )
        {
            application.passivate();
        }
    }
}