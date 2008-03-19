package org.qi4j.library.framework.locking;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;

/**
 * TODO
 */
@Concerns( { ReadLockConcern.class, WriteLockConcern.class } )
@Mixins( ReentrantReadWriteLock.class )
public interface LockingAbstractComposite
{
}
