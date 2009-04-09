package org.qi4j.library.locking;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JAVADOC
 */
@Concerns( { ReadLockConcern.class, WriteLockConcern.class } )
@Mixins( ReentrantReadWriteLock.class )
public interface LockingAbstractComposite
{
}
