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

package org.apache.zest.test;

import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractZestTest extends AbstractZestBaseTest
    implements Assembler
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    protected TransientBuilderFactory transientBuilderFactory;

    @Structure
    protected ValueBuilderFactory valueBuilderFactory;

    @Structure
    protected ServiceFinder serviceFinder;

    @Structure
    protected ObjectFactory objectFactory;

    @Structure
    protected QueryBuilderFactory queryBuilderFactory;

    @Structure
    protected ModuleDescriptor module;

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
        Module module = application.findModule( "Layer 1", "Module 1" );
        module.injectTo( this );
    }

    @Override
    protected void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly layer = applicationAssembly.layer( "Layer 1" );
        ModuleAssembly module = layer.module( "Module 1" );
        new DefaultUnitOfWorkAssembler().assemble( module );
        module.objects( AbstractZestTest.this.getClass() );
        assemble( module );
    }

    @After
    @Override
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