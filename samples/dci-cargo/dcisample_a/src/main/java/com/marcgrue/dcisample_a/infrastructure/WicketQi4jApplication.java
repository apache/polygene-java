package com.marcgrue.dcisample_a.infrastructure;

import com.marcgrue.dcisample_a.infrastructure.conversion.EntityToDTOService;
import com.marcgrue.dcisample_a.infrastructure.dci.Context;
import com.marcgrue.dcisample_a.infrastructure.model.Queries;
import com.marcgrue.dcisample_a.infrastructure.model.ReadOnlyModel;
import com.marcgrue.dcisample_a.infrastructure.wicket.page.BaseWebPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Base Wicket Web Application containing the Qi4j application.
 */
public class WicketQi4jApplication
      extends WebApplication
{
    public Logger logger = LoggerFactory.getLogger( WicketQi4jApplication.class );

    protected ApplicationSPI qi4jApp;
    protected Module qi4jModule;

    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    protected ValueBuilderFactory vbf;

    @Structure
    protected QueryBuilderFactory qbf;

    @Structure
    protected TransientBuilderFactory tbf;

    @Service
    protected EntityToDTOService valueConverter;


    /**
     * Qi4j Assembler
     *
     * To let the custom application class (DCISampleApplication_x) focus on starting up the
     * Wicket environment, I made a convention of having Qi4j Assembler files in an 'assembly'
     * folder beside the custom application class.
     *
     * There's always only one application file, but we could split the assemblage into several
     * files ie. one for each layer. In that case, the Assembler file would be distributing to
     * the individual LayerXAssembler classes.
     *
     * If you like, you can also override this method in the custom application class and simply
     * return an instance of YourAssembler:
     *
     * @Override
     * protected ApplicationAssembler getAssembler() {
     *     return new YourAssemblerInAnyPath();
     * }
     */
    protected ApplicationAssembler getAssembler() throws Exception
    {
        String appPath = getClass().getCanonicalName();
        String expectedPathFromApplication = ".assembly.Assembler";
        String assemblerPath = appPath.substring( 0, appPath.lastIndexOf( "." ) ) + expectedPathFromApplication;
        try
        {
            return (ApplicationAssembler) Class.forName( assemblerPath ).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new Exception( "Couldn't find Qi4j assembler in path '" + assemblerPath + "'" );
        }
    }
    protected String defaultLayerName()
    {
        return "BOOTSTRAP";
    }
    protected String defaultModuleName()
    {
        return "BOOTSTRAP-Bootstrap";
    }

    // Override this to bootstrap the wicket application
    protected void wicketInit()
    {}

    @Override
    protected void init()
    {
        startQi4j();
        handleUnitOfWork();

        Context.prepareContextBaseClass( uowf );
        BaseWebPage.prepareBaseWebPageClass( tbf );
        ReadOnlyModel.prepareModelBaseClass( uowf, vbf, valueConverter );
        Queries.prepareQueriesBaseClass( uowf, qbf );

        wicketInit();
    }

    private void startQi4j()
    {
        try
        {
            logger.info( "Starting Qi4j application" );
            Energy4Java qi4j = new Energy4Java();
            qi4jApp = qi4j.newApplication( getAssembler() );
            qi4jApp.activate();
            qi4jModule = qi4jApp.findModule( defaultLayerName(), defaultModuleName() );

            // Qi4j injects @Structure and @Service elements into this application instance
            qi4jModule.objectBuilderFactory().newObjectBuilder( WicketQi4jApplication.class ).injectTo( this );

            logger.info( "Started Qi4j application" );
        }
        catch (Exception e)
        {
            logger.error( "Could not start Qi4j application." );
            e.printStackTrace();
            System.exit( 100 );
        }
    }

    private void handleUnitOfWork()
    {
        getRequestCycleListeners().add( new AbstractRequestCycleListener()
        {
            @Override
            public void onBeginRequest( final RequestCycle requestCycle )
            {
                super.onBeginRequest( requestCycle );

                logger.debug( "================================" );
                logger.debug( "REQUEST start" );
                logger.debug( requestCycle.getRequest().toString() );
                logger.debug( requestCycle.getRequest().getRequestParameters().toString() );

                UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "REQUEST" ) );
                logger.debug( "  ### NEW " + uow + "   ### MODULE: " + qi4jModule );
            }

            @Override
            public void onEndRequest( final RequestCycle requestCycle )
            {
                UnitOfWork uow = uowf.currentUnitOfWork();
                if (uow != null)
                {
                    try
                    {
                        if ("POST".equals( ( (HttpServletRequest) requestCycle.getRequest().getContainerRequest() ).getMethod() ))
                        {
                            // "Save"
                            logger.debug( "  ### COMPLETE " + uow + "   ### MODULE: " + qi4jModule );
                            uow.complete();
                        }
                        else
                        {
                            // GET requests
                            logger.debug( "  ### DISCARD " + uow + "   ### MODULE: " + qi4jModule );
                            uow.discard();
                        }
                    }
                    catch (ConcurrentEntityModificationException e)
                    {
                        logger.error( "  ### DISCARD " + uow + "   ### MODULE: " + qi4jModule );
                        uow.discard();
                        e.printStackTrace();
                    }
                    catch (UnitOfWorkCompletionException e)
                    {
                        logger.error( "  ### DISCARD " + uow + "   ### MODULE: " + qi4jModule );
                        uow.discard();
                        e.printStackTrace();
                    }
                }
                logger.debug( "REQUEST end" );
                logger.debug( "------------------------------------" );
            }
        } );
    }

    // Since Qi4j can only add concrete classes in the assembly, we need to implement a (dummy) getHomePage()
    // method here. Override in wicket application class with a real returned page class.
    @Override
    public Class<? extends Page> getHomePage()
    {
        return null;
    }

    @Override
    protected void onDestroy()
    {
        if (qi4jApp == null)
            return;

        try
        {
            logger.info( "Passivating Qi4j application" );
            qi4jApp.passivate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String appVersion()
    {
        return qi4jApp.version();
    }
}