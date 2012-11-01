package org.qi4j.test;

import org.junit.After;
import org.junit.Before;
import org.qi4j.api.Qi4j;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.Qi4jSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQi4jBaseTest
{
    protected Qi4j api;
    protected Qi4jSPI spi;

    protected Energy4Java qi4j;
    protected ApplicationDescriptor applicationModel;
    protected Application application;

    private Logger log;

    @Before
    public void setUp()
        throws Exception
    {
        qi4j = new Energy4Java();
        applicationModel = newApplication();
        if( applicationModel == null )
        {
            // An AssemblyException has occurred that the Test wants to check for.
            return;
        }
        application = newApplicationInstance( applicationModel );
        initApplication( application );
        api = spi = qi4j.spi();
        application.activate();
    }

    /** Called by the superclass for the test to define the entire application, every layer, every module and all
     * the contents of each module.
     *
     * @param applicationAssembly the {@link org.qi4j.bootstrap.ApplicationAssembly} to be populated.
     */
    protected abstract void defineApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException;

    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        return applicationModel.newInstance( qi4j.api() );
    }

    protected ApplicationDescriptor newApplication()
        throws AssemblyException
    {
        ApplicationAssembler assembler = new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
                applicationAssembly.setMode( Application.Mode.test );
                defineApplication( applicationAssembly );
                return applicationAssembly;
            }
        };

        try
        {
            return qi4j.newApplicationModel( assembler );
        }
        catch( AssemblyException e )
        {
            assemblyException( e );
            return null;
        }
    }

    /**
     * This method is called when there was an AssemblyException in the creation of the Qi4j application model.
     * <p/>
     * Override this method to catch valid failures to place into satisfiedBy suites.
     *
     * @param exception the exception thrown.
     *
     * @throws org.qi4j.bootstrap.AssemblyException The default implementation of this method will simply re-throw the exception.
     */
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        throw exception;
    }

    protected void initApplication( Application app )
        throws Exception
    {
    }

    @After
    public void tearDown()
        throws Exception
    {
        if( application != null )
        {
            application.passivate();
        }
    }

    protected Logger getLog()
    {
        if( this.log == null )
        {
            this.log = LoggerFactory.getLogger( this.getClass() );
        }

        return this.log;
    }
}
