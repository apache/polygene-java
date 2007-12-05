package org.qi4j.library.framework.caching;

import org.qi4j.annotation.Concerns;
import org.qi4j.annotation.Mixins;
import org.qi4j.annotation.SideEffects;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
@Mixins( InvocationCacheMixin.class )
@Concerns( ReturnCachedValueOnExceptionConcern.class )
@SideEffects( { CacheInvocationResultSideEffect.class, InvalidateCacheOnSettersSideEffect.class } )
public interface InvocationCacheAbstractComposite
    extends Composite
{
}
