package org.qi4j.library.locking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.This;

/**
 * Applies read-lock to Composite
 */
@AppliesTo( ReadLock.class )
public class ReadLockConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    private @This ReadWriteLock lock;

    public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
    {
        lock.readLock().lock();
        try
        {
            return next.invoke( o, method, objects );
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
}
