/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.bootstrap.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.sample.dcicargo.sample_a.bootstrap.sampledata.BaseData;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.dci.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for testing Context Interactions
 */
public class TestApplication extends BaseData
{
    // Logger for sub classes
    protected Logger logger = LoggerFactory.getLogger( getClass() );

    protected static Application app;

    @BeforeClass
    public static void setup() throws Exception
    {
        System.out.println( "\n@@@@@@@@@@@@@@@  TEST  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" );
        app = new Energy4Java().newApplication( new TestAssembler() );
        app.activate();

        // Separate test suites in console output
        System.out.println();
    }

    public TestApplication()
    {
        super(app.findModule( "BOOTSTRAP", "BOOTSTRAP-Bootstrap" ));
        Context.prepareContextBaseClass( module );
    }

    // Printing current test method name to console
    @Rule
    public TestName name = new TestName();

    @Before
    public void prepareTest()
        throws Exception
    {
        logger.info( name.getMethodName() );
        Usecase usecase = UsecaseBuilder.newUsecase( "Usecase: " + name );
        module.newUnitOfWork(usecase);
    }

    @After
    public void concludeTest()
    {
        UnitOfWork uow = module.currentUnitOfWork();
        if( uow != null && uow.isOpen() )
            uow.discard();
        if( module != null && module.isUnitOfWorkActive() )
        {
            while( module.isUnitOfWorkActive() )
            {
                uow = module.currentUnitOfWork();
                if( uow.isOpen() )
                {
                    System.err.println( "UnitOfWork not cleaned up:" + uow.usecase().name() );
                    uow.discard();
                }
                else
                {
                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened. First is: " + uow.usecase().name() );
                }
            }
            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }

    }

    @AfterClass
    public static void terminateApplication()
        throws PassivationException
    {
        if( app != null )
        {
            app.passivate();
        }
    }
}
