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
 *
 */

package org.apache.zest.test.indexing.layered;

import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.layered.ModuleAssembler;
import org.apache.zest.test.indexing.TestData;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractMultiLayeredIndexingTest
{
    static Class<? extends ModuleAssembler> indexingAssembler;

    protected Application application;
    private Iterable<ServiceReference<TestCase>> suite1;
    private Iterable<ServiceReference<TestCase>> suite2;
    private Iterable<ServiceReference<TestCase>> suite3;

    public AbstractMultiLayeredIndexingTest( Class<? extends ModuleAssembler> indexingAssembler )
    {
        AbstractMultiLayeredIndexingTest.indexingAssembler = indexingAssembler;
    }

    @Before
    public void setup()
        throws AssemblyException, ActivationException
    {
        ApplicationAssembler assembler =
            new ApplicationAssembler( "Multi Layered Indexing Test", "1.0", Application.Mode.development );
        assembler.initialize();
        assembler.start();
        application = assembler.application();
        Module familyModule = application.findModule( "Domain Layer", "Family Module" );
        TestData.populate( familyModule );
        Module suite1Module = application.findModule( "Access Layer", "TestSuite1 Module" );
        suite1 = suite1Module.findServices( TestCase.class );

        Module suite2Module = application.findModule( "Access Layer", "TestSuite2 Module" );
        suite2 = suite2Module.findServices( TestCase.class );

        Module suite3Module = application.findModule( "Access Layer", "TestSuite3 Module" );
        suite3 = suite3Module.findServices( TestCase.class );
    }

    @Test
    public void suite1Tests()
        throws Exception
    {
        Iterable<ServiceReference<TestCase>> suite = this.suite1;
        runTest( suite );
    }

    @Test
    public void suite2Tests()
        throws Exception
    {
        runTest( suite2 );
    }

    @Test
    public void suite3Tests()
        throws Exception
    {
        runTest( suite3 );
    }

    private void runTest( Iterable<ServiceReference<TestCase>> suite )
        throws Exception
    {
        for( ServiceReference<TestCase> ref : suite )
        {
            TestCase testCase = ref.get();
            testCase.when();
            testCase.given();
            testCase.expect();
        }
    }
}
