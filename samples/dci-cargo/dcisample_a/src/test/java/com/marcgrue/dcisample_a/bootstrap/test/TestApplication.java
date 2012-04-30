package com.marcgrue.dcisample_a.bootstrap.test;

import com.marcgrue.dcisample_a.bootstrap.sampledata.BaseData;
import com.marcgrue.dcisample_a.infrastructure.dci.Context;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for testing Context Interactions
 */
public class TestApplication
      extends BaseData
{
    // Logger for sub classes
    protected Logger logger = LoggerFactory.getLogger( getClass() );

    protected static ApplicationSPI app;
    protected static UnitOfWorkFactory uowf;

    @BeforeClass
    public static void setup() throws Exception
    {
        System.out.println( "\n@@@@@@@@@@@@@@@  TEST  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" );
        app = new Energy4Java().newApplication( new TestAssembler() );
        app.activate();

        uowf = app.findModule( "BOOTSTRAP", "BOOTSTRAP-Bootstrap" ).unitOfWorkFactory();

        Context.prepareContextBaseClass( uowf );

        // Separate test suites in console output
        System.out.println();
    }

    // Printing current test method name to console
    @Rule
    public TestName name = new TestName();
    @Before
    public void printCurrentTestMethodName()
    {
        logger.info( name.getMethodName() );
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (uow != null)
        {
            uow.discard();
            uow = null;
        }

        if (uowf != null && uowf.currentUnitOfWork() != null)
        {
            UnitOfWork current;
            while (( current = uowf.currentUnitOfWork() ) != null)
            {
                if (current.isOpen())
                {
                    current.discard();
                }
                else
                {
                    throw new InternalError( "UoW is on the stack, but not opened." );
                }
            }

            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }

        if (app != null)
        {
            app.passivate();
        }
    }
}