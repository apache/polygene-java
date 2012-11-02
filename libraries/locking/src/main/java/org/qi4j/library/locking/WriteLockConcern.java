package org.qi4j.library.locking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;

/**
 * Applies write-lock to Composite
 */
@AppliesTo( WriteLock.class )
public class WriteLockConcern
    extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private
    @This
    ReadWriteLock lock;

    @Override
    public Object invoke( Object o, Method method, Object[] objects )
        throws Throwable
    {
        Lock writeLock = lock.writeLock();

        lock(writeLock);
        try
        {
            return next.invoke( o, method, objects );
        }
        finally
        {
            try
            {
                writeLock.unlock();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fix for this bug:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6822370
     */
    protected void lock(Lock lock)
    {
        while(true)
        {
            try
            {
                while( !(lock.tryLock() || lock.tryLock( 1000, TimeUnit.MILLISECONDS )) )
                {
                    // On timeout, try again
                }
                return; // Finally got a lock
            }
            catch( InterruptedException e )
            {
                // Try again
            }
        }
    }
}