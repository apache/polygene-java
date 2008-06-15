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

package org.qi4j.test;

import org.junit.After;
import org.junit.Before;
import org.qi4j.Qi4j;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.ApplicationFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.runtime.Energy4Java;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.service.ServiceFinder;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.structure.Application;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractQi4jTest
    implements Assembler
{
    protected Qi4j api;
    protected Qi4jSPI spi;
    protected Qi4jRuntime runtime;

    protected ApplicationFactory applicationFactory;
    protected Application application;

    protected CompositeBuilderFactory compositeBuilderFactory;
    protected ObjectBuilderFactory objectBuilderFactory;
    protected UnitOfWorkFactory unitOfWorkFactory;
    protected ServiceFinder serviceLocator;

    protected ModuleInstance moduleInstance;

    @Before public void setUp() throws Exception
    {
        api = spi = runtime = new Energy4Java();
        applicationFactory = new ApplicationFactory( runtime, new ApplicationAssemblyFactory() );
        application = newApplication();
        application.activate();

        // Assume only one module
        moduleInstance = (ModuleInstance) application.findModule( "Layer 1", "Module 1" );
        compositeBuilderFactory = moduleInstance.compositeBuilderFactory();
        objectBuilderFactory = moduleInstance.objectBuilderFactory();
        unitOfWorkFactory = moduleInstance.unitOfWorkFactory();
        serviceLocator = moduleInstance.serviceFinder();
    }

    protected Application newApplication()
        throws AssemblyException
    {
        return applicationFactory.newApplication( this );
    }

    @After public void tearDown() throws Exception
    {
        if( unitOfWorkFactory != null && unitOfWorkFactory.currentUnitOfWork() != null )
        {
            UnitOfWork current;
            while( ( current = unitOfWorkFactory.currentUnitOfWork() ) != null )
            {
                if( current.isOpen() )
                {
                    current.discard();
                }
                else
                {
                    System.out.println( "Internal Error" );
                }
            }

            throw new Exception( "UnitOfWork not properly cleaned up" );
        }

        if( application != null )
        {
            application.passivate();
        }
    }

}