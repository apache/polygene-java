package org.apache.polygene.library.execution;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.execution.assembly.ExecutionServiceAssembler;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ExecutionServiceTest extends AbstractPolygeneTest
{

    private CopyOnWriteArraySet<Thread> threads = new CopyOnWriteArraySet<>();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new ExecutionServiceAssembler()
            .withMaxThreads( 3 )
            .assemble( module );
    }

    @Test
    void givenMaxThreeThreadsWhenSubmittingManyTasksExpectToOnlySeeThreeThreads()
        throws InterruptedException
    {
        ExecutorService underTest = serviceFinder.findService( ExecutorService.class ).get();
        Runnable r = () -> {
            threads.add( Thread.currentThread() );
        };
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        underTest.submit( r );
        Thread.sleep( 10 );
        assertThat( threads.size(), equalTo( 3 ) );
        underTest.shutdownNow();
    }
}
