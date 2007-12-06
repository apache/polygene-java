package org.qi4j.library.framework.caching;

import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.SideEffects;

/**
 * TODO
 */
@Mixins( InvocationCacheMixin.class )
@Concerns( ReturnCachedValueOnExceptionConcern.class )
@SideEffects( { CacheInvocationResultSideEffect.class, InvalidateCacheOnSettersSideEffect.class } )
public interface InvocationCacheAbstractComposite
{
}
