package org.apache.polygene.library.execution.assembly;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.ServiceImporterException;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;

public class ExecutionServiceAssembler extends Assemblers.VisibilityIdentityConfig<ExecutionServiceAssembler>
    implements Assembler
{

    private ThreadFactory factory;
    private RejectedExecutionHandler rejectedExecutionHandler;
    private int coreThreads = 3;
    private int maxThreads = 10;
    private int maxTasks = 1000;
    private long time = 1000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private ThreadGroup group;
    private BlockingQueue<Runnable> queue;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.importedServices( ExecutorService.class )
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
    public ExecutionServiceAssembler withCoreThreads( int threads )
    // END SNIPPET: configuration
    {
        this.coreThreads = threads;
        return this;
    }

    /**
     * Maximum number of threads to be used.
     *
     * @param threads max threads to use
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Maximum number of threads to be used.
    public ExecutionServiceAssembler withMaxThreads( int threads )
    // END SNIPPET: configuration
    {
        this.maxThreads = threads;
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
    public ExecutionServiceAssembler withThreadFactory( ThreadFactory factory )
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
    public ExecutionServiceAssembler withRejectedExecutionHandler( RejectedExecutionHandler handler )
    // END SNIPPET: configuration
    {
        this.rejectedExecutionHandler = handler;
        return this;
    }

    /**
     * Max number of entries in queue.
     * <p>
     * Blocking occurs if more submissions are given.
     * </p>
     * <p>
     * If a custom queue is defined, then this parameter will be ignored.
     * </p>
     *
     * @param maxTasks Max number of tasks that can be added to the queue before blocking occurs.
     * @return Fluent API
     * @see LinkedBlockingQueue which is backing the {@link ThreadPoolExecutor} if a custom one is not provided.
     */
    // START SNIPPET: configuration
    // Max number of entries in queue. Blocking occurs if more submissions given
    public ExecutionServiceAssembler withMaxQueueSize( int maxTasks )
    // END SNIPPET: configuration
    {
        this.maxTasks = maxTasks;
        return this;
    }

    /**
     * Provide custom queue.
     * <p>
     * If a custom queue is defined, then the {@link #withMaxQueueSize(int)} parameter will be ignored.
     * </p>
     *
     * @param queue The custom queue to use.
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // Provide custom queue. If used, the withMaxQueueSize is ignored
    public ExecutionServiceAssembler withQueue( BlockingQueue<Runnable> queue )
    // END SNIPPET: configuration
    {
        this.queue = queue;
        return this;
    }

    /**
     * For how long the threads should be kept around idling before discarded
     *
     * @param time The time to keep alive
     * @param unit The unit in which the 'time' argument is expressed.
     * @return Fluent API
     */
    // START SNIPPET: configuration
    // For how long the threads should be kept around idling before discarded
    public ExecutionServiceAssembler withKeepAliveTime( int time, TimeUnit unit )
    // END SNIPPET: configuration
    {
        this.time = time;
        this.unit = unit;
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
    public ExecutionServiceAssembler inThreadGroup( ThreadGroup group )
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
            ExecutionServiceAssembler metaInfo = serviceDescriptor.metaInfo( ExecutionServiceAssembler.class );
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
            BlockingQueue<Runnable> queue;
            if( metaInfo.queue == null )
            {
                queue = new LinkedBlockingQueue<>( metaInfo.maxTasks );
            }
            else
            {
                queue = metaInfo.queue;
            }
            if( metaInfo.rejectedExecutionHandler == null )
            {
                return new ThreadPoolExecutor( metaInfo.coreThreads,
                                               metaInfo.maxThreads,
                                               metaInfo.time,
                                               metaInfo.unit,
                                               queue,
                                               factory
                );
            }
            else
            {
                return new ThreadPoolExecutor( metaInfo.coreThreads,
                                               metaInfo.maxThreads,
                                               metaInfo.time,
                                               metaInfo.unit,
                                               queue,
                                               factory,
                                               metaInfo.rejectedExecutionHandler
                );
            }
        }

        @Override
        public boolean isAvailable( Object instance )
        {
            return false;
        }
    }
}
