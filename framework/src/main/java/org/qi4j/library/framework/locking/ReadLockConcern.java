package org.qi4j.library.framework.locking;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReadWriteLock;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.ThisCompositeAs;

/**
 * Applies read-lock to Composite
 */
@AppliesTo( ReadLock.class )
public class ReadLockConcern
    implements InvocationHandler
{
    private @ConcernFor InvocationHandler next;
    private @ThisCompositeAs ReadWriteLock lock;

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
