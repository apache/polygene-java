package org.apache.polygene.library.execution;

import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;

// TODO: These tests are extremely slow. Why is that? Could it be the streaming of exception class hierarchies?
public class RetryTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( TestService.class ).withConcerns( RetryConcern.class ).instantiateOnStartup();
    }

    @Test
    void givenMethodThrowingExceptionWhenCallingExpectFourCalls()
    {
        TestService underTest = serviceFinder.findService( TestService.class ).get();
        long start = System.currentTimeMillis();
        try
        {
            underTest.doWithRetry1();
        }
        catch( IllegalStateException e )
        {
            // expected after 4 calls
        }
        long end = System.currentTimeMillis();
        assertThat( underTest.calledTimes(), equalTo(4));
        assertThat( end-start, lessThan(20L));
    }

    @Test
    void givenMethodThrowingExceptionWhenCallingExpectFiveCalls()
    {
        TestService underTest = serviceFinder.findService( TestService.class ).get();
        long start = System.currentTimeMillis();
        try
        {
            underTest.doWithRetry2();
        }
        catch( IllegalStateException e )
        {
            // expected after 4 calls
        }
        long end = System.currentTimeMillis();
        assertThat( underTest.calledTimes(), equalTo(5));
        assertThat( end-start, lessThan(20L));
    }

    @Test
    void givenMethodThrowingExceptionWhenCallingExpectSixCalls()
    {
        TestService underTest = serviceFinder.findService( TestService.class ).get();
        long start = System.currentTimeMillis();
        try
        {
            underTest.doWithRetry3();
        }
        catch( IllegalStateException e )
        {
            // expected after 4 calls
        }
        long end = System.currentTimeMillis();
        assertThat( underTest.calledTimes(), equalTo(6));
        assertThat( end-start, lessThan(20L));
    }

    @Test
    void givenMethodThrowingExceptionWhenCallingExpectOneTries()
    {
        TestService underTest = serviceFinder.findService( TestService.class ).get();
        long start = System.currentTimeMillis();
        try
        {
            underTest.doWithRetry4();
        }
        catch( IllegalStateException e )
        {
            // expected after 1 calls, since IllegalStateException is an "unless"
        }
        long end = System.currentTimeMillis();
        assertThat( underTest.calledTimes(), equalTo(1));
        assertThat( end-start, lessThan(20L));
    }

    @Test
    void givenBackoffExceptionWhenCallingExpectSlowTries()
    {
        TestService underTest = serviceFinder.findService( TestService.class ).get();
        long start = System.currentTimeMillis();
        try
        {
            underTest.doWithRetry5();
        }
        catch( IllegalStateException e )
        {
            // expected after 1 calls, since IllegalStateException is an "unless"
        }
        long end = System.currentTimeMillis();
        assertThat( underTest.calledTimes(), equalTo(3));
        assertThat( end-start, greaterThanOrEqualTo( 300L));
    }


    @Mixins( TestMixin.class)
    public interface TestService{

        int calledTimes();

        @Retry(4)
        void doWithRetry1();

        @Retry( value=5, on = IllegalStateException.class )
        void doWithRetry2();

        @Retry( value=6, on = IllegalStateException.class )
        void doWithRetry3();

        @Retry( value=7, unless = IllegalStateException.class )
        void doWithRetry4();

        @Retry( value = 3, backoff = 100 )
        void doWithRetry5();
    }

    public class TestMixin
        implements TestService
    {

        private int called = 0;

        @Override
        public int calledTimes()
        {
            return called;
        }

        @Override
        public void doWithRetry1()
        {
            called++;
            throw new IllegalStateException();
        }

        @Override
        public void doWithRetry2()
        {
            called++;
            throw new IllegalStateException();
        }

        @Override
        public void doWithRetry3()
        {
            called++;
            throw new IllegalStateException();
        }

        @Override
        public void doWithRetry4()
        {
            called++;
            throw new IllegalStateException();
        }

        @Override
        public void doWithRetry5()
        {
            called++;
            throw new IllegalStateException();
        }
    }
}
