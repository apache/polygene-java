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
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractQi4jTest extends AbstractQi4jBaseTest
    implements Assembler
{
    protected Module module;

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        if( application == null )
        {
            return; // failure in Assembly.
        }
        module = application.findModule( "Layer 1", "Module 1" );
        module.injectTo( this );
    }

    @Override
    protected void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly layer = applicationAssembly.layer( "Layer 1" );
        ModuleAssembly module = layer.module( "Module 1" );
        module.objects( AbstractQi4jTest.this.getClass() );
        assemble( module );
    }

    @After
    @Override
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
                    System.err.println( "UnitOfWork not cleaned up:" + uow.usecase().name() );
                    uow.discard();
                }
                else
                {
                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened. First is: " + uow
                        .usecase()
                        .name() );
                }
            }
            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }
        super.tearDown();
    }
}