package org.apache.polygene.library.restlet;

import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.PassivationException;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.layered.LayeredApplicationAssembler;

@SuppressWarnings( "WeakerAccess" )
public abstract class PolygeneRestApplicationLauncher
{
    protected org.apache.polygene.api.structure.Application polygeneApplication;
    protected ServiceFinder serviceFinder;
    protected ObjectFactory objectFactory;
    protected TransientBuilderFactory transientBuilderFactory;
    protected UnitOfWorkFactory unitOfWorkFactory;
    protected ValueBuilderFactory valueBuilderFactory;
    protected Module entryModule;

    protected void initialize()
        throws ActivationException
    {
        polygeneApplication = createApplication();
        activateApplication();
        entryModule = polygeneApplication.findModule( entryLayer(), entryModule() );
        serviceFinder = entryModule;
        objectFactory = entryModule;
        transientBuilderFactory = entryModule;
        unitOfWorkFactory = entryModule.unitOfWorkFactory();
        valueBuilderFactory = entryModule;
    }

    protected abstract String entryLayer();

    protected abstract String entryModule();

    protected void activateApplication()
        throws ActivationException
    {
        polygeneApplication.activate();
    }

    protected Application createApplication()
    {
        try
        {
            LayeredApplicationAssembler assembler = createApplicationAssembler();
            assembler.initialize();
            return assembler.application();
        }
        catch( Throwable e )
        {
            throw new RuntimeException( "Unable to start Polygene application.", e );
        }
    }

    protected abstract LayeredApplicationAssembler createApplicationAssembler()
        throws AssemblyException;

    protected void installShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( this::shutdown ) );
    }

    protected void shutdown()
    {
        try
        {
            if( polygeneApplication != null )
            {
                polygeneApplication.passivate();
            }
        }
        catch( PassivationException e )
        {
            throw new RuntimeException( "Unable to shut down cleanly.", e );
        }
    }
}
