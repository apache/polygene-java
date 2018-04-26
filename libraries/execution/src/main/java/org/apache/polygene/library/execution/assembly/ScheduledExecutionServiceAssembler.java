package org.apache.polygene.library.execution.assembly;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.ServiceImporterException;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;

public class ScheduledExecutionServiceAssembler extends Assemblers.VisibilityIdentityConfig<ScheduledExecutionServiceAssembler>
    implements Assembler
{

    private ThreadFactory factory;
    private RejectedExecutionHandler rejectedExecutionHandler;
    private int coreThreads = 3;
    private ThreadGroup group;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importedServices( ScheduledExecutorService.class )
              .importedBy( ThreadPoolExecutorImporter.class )
              .setMetaInfo( this )
        ;
    }

    /**
     * Number of core threads, i.e. threads that are not removed if idle, to be used.
     *
     * @param threads core threads to use
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Number of core threads, i.e. threads that are not removed if idle, to be used.
    public ScheduledExecutionServiceAssembler withCoreThreads( int threads )
    // END SNIPPET: configuration
    {
        this.coreThreads = threads;
        return this;
    }

    /**
     * Provide a custom ThreadFactory.
     * <p>
     * If defined, the {@link #inThreadGroup(ThreadGroup)} parameter will be ignored.
     * </p>
     *
     * @param factory The thread factory to use, when creating threads.
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Provide a custom ThreadFactory. If defined, the inThreadGroup parameter will be ignored.
    public ScheduledExecutionServiceAssembler withThreadFactory( ThreadFactory factory )
    // END SNIPPET: configuration
    {
        this.factory = factory;
        return this;
    }

    /**
     * Provide a custom {@link RejectedExecutionHandler}, or one of the pre-defined policies
     *
     * @param handler the custom {@link RejectedExecutionHandler} to use.
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Provide a custom RejectedExecutionHandler, or one of the pre-defined policies
    public ScheduledExecutionServiceAssembler withRejectedExecutionHandler( RejectedExecutionHandler handler )
    // END SNIPPET: configuration
    {
        this.rejectedExecutionHandler = handler;
        return this;
    }

    /**
     * Thread Group to create the threads in.
     * <p>
     * If a custom ThreadFactory is given, {@link #withThreadFactory(ThreadFactory)} then this parameter is ignored.
     * </p>
     *
     * @param group The thread group that all threads should be placed in.
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Thread Group to create the threads in
    public ScheduledExecutionServiceAssembler inThreadGroup( ThreadGroup group )
    // END SNIPPET: configuration
    {
        this.group = group;
        return this;
    }

    private static class ThreadPoolExecutorImporter
        implements ServiceImporter
    {
        long count = 0;

        @Override
        public Object importService( ImportedServiceDescriptor serviceDescriptor )
            throws ServiceImporterException
        {
            ScheduledExecutionServiceAssembler metaInfo = serviceDescriptor.metaInfo( ScheduledExecutionServiceAssembler.class );
            ThreadGroup group;
            if( metaInfo.group == null )
            {
                group = new ThreadGroup( "tg-" + metaInfo.identity() );
            }
            else
            {
                group = metaInfo.group;
            }
            ThreadFactory factory = metaInfo.factory;
            if( factory == null )
            {
                factory = runnable -> new Thread( group, runnable, "t-" + count++ );
            }
            if( metaInfo.rejectedExecutionHandler == null )
            {
                return new ScheduledThreadPoolExecutor( metaInfo.coreThreads, factory );
            }
            else
            {
                return new ScheduledThreadPoolExecutor( metaInfo.coreThreads, factory, metaInfo.rejectedExecutionHandler );
            }
        }

        @Override
        public boolean isAvailable( Object instance )
        {
            return false;
        }
    }
}
