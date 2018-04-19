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

import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Composite tests.
 */
public abstract class AbstractPolygeneTest extends AbstractPolygeneBaseTest
    implements Assembler
{
    @Structure
    protected UnitOfWorkFactory unitOfWorkFactory;

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

    @Override
    @BeforeEach
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
    {
        LayerAssembly layer = applicationAssembly.layer( "Layer 1" );
        ModuleAssembly module = layer.module( "Module 1" );
        module.objects( AbstractPolygeneTest.this.getClass() );
        assemble( module );
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        if( unitOfWorkFactory != null && unitOfWorkFactory.isUnitOfWorkActive() )
        {
            while( unitOfWorkFactory.isUnitOfWorkActive() )
            {
                UnitOfWork uow = unitOfWorkFactory.currentUnitOfWork();
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