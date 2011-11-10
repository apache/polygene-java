package org.qi4j.library.locking;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Concerns( { ReadLockConcern.class, WriteLockConcern.class } )
@Mixins( LockMixin.class )
public interface LockingAbstractComposite
{
}
