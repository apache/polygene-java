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

package org.apache.polygene.test.indexing.layered;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.service.qualifier.Tagged;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.layered.ModuleAssembler;
import org.apache.polygene.test.indexing.TestData;
import org.apache.polygene.test.model.assembly.ApplicationAssembler;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractMultiLayeredIndexingTest
{
    public static Class<? extends ModuleAssembler> indexingAssembler;

    protected Application application;

    @Structure
    private UnitOfWorkFactory uowf;

    @Optional
    @Service
    @Tagged( "Suite1Case1" )
    private ServiceReference<TestCase> suite1Case1;

    @Optional
    @Service
    @Tagged( "Suite1Case2" )
    private ServiceReference<TestCase> suite1Case2;

    @Optional
    @Service
    @Tagged( "Suite2Case1" )
    private ServiceReference<TestCase> suite2Case1;

    @Optional
    @Service
    @Tagged( "Suite3Case1" )
    private ServiceReference<TestCase> suite3Case1;

    public AbstractMultiLayeredIndexingTest( Class<? extends ModuleAssembler> indexingAssembler )
    {
        AbstractMultiLayeredIndexingTest.indexingAssembler = indexingAssembler;
    }

    @Before
    public void setup()
        throws AssemblyException, ActivationException
    {
        ApplicationAssembler assembler =
            new ApplicationAssembler( "Multi Layered Indexing Test", "1.0", Application.Mode.development, getClass() );
        assembler.initialize();
        assembler.start();
        application = assembler.application();
        Module familyModule = application.findModule( "Domain Layer", "Family Module" );
        TestData.populate( familyModule );
        Module executionModule = application.findModule( "Access Layer", "TestExecution Module" );
        executionModule.injectTo( this );
    }

    @Test
    public void suite1Case1()
        throws Exception
    {
        runTest( suite1Case1, "suite1Case1" );
    }

    @Test
    public void suite1Case2()
        throws Exception
    {
        runTest( suite1Case2, "suite1Case2"  );
    }

    @Test
    public void suite2Case1()
        throws Exception
    {
        runTest( suite2Case1, "suite2Case1"  );
    }

    @Test
    public void suite3Case1()
        throws Exception
    {
        runTest( suite3Case1, "suite3Case1"  );
    }

    private void runTest( ServiceReference<TestCase> testCaseRef, String testName )
        throws Exception
    {
        if( testCaseRef == null )
        {
            System.err.println( "TestCase is not defined." );
        }
        else
        {
            TestCase testCase = testCaseRef.get();
            try(UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( testName ) ))
            {
                testCase.given();
                testCase.when();
                testCase.expect();
                uow.complete();
            }
        }
    }
}
