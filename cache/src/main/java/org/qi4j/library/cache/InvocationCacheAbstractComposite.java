package org.qi4j.library.cache;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;

/**
 * TODO
 */
@Mixins( InvocationCacheMixin.class )
@Concerns( ReturnCachedValueOnExceptionConcern.class )
@SideEffects( { CacheInvocationResultSideEffect.class, InvalidateCacheOnSettersSideEffect.class } )
public interface InvocationCacheAbstractComposite
{
}
