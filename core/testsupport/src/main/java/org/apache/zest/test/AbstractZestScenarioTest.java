/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.apache.zest.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.spi.ZestSPI;

/**
 * Base class for Zest scenario tests. This will create one Zest application per class instead of per test.
 */
public abstract class AbstractZestScenarioTest
    implements Assembler
{
    static protected ZestAPI api;
    static protected ZestSPI spi;

    static protected Energy4Java zest;
    static protected ApplicationDescriptor applicationModel;
    static protected Application application;

    static protected Module module;

    static protected Assembler assembler; // Initialize this in static block of subclass

    @BeforeClass
    public static void setUp()
        throws Exception
    {
        zest = new Energy4Java();
        applicationModel = newApplication();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = applicationModel.newInstance( zest.spi() );
        initApplication( application );
        api = spi = zest.spi();
        application.activate();

        // Assume only one module
        module = application.findModule( "Layer 1", "Module 1" );
    }

    static protected ApplicationDescriptor newApplication()
        throws AssemblyException
    {
        final Assembler asm = assembler;

        ApplicationAssembler assembler = new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                return applicationFactory.newApplicationAssembly( asm );
            }
        };
        try
        {
            return zest.newApplicationModel( assembler );
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
     *
     * @param exception the exception thrown.
     *
     * @throws org.apache.zest.bootstrap.AssemblyException The default implementation of this method will simply re-throw the exception.
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
        if( module != null && module.isUnitOfWorkActive() )
        {
            while( module.isUnitOfWorkActive() )
            {
                UnitOfWork uow = module.currentUnitOfWork();
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