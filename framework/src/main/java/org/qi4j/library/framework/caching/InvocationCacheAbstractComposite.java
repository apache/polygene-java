package org.qi4j.library.framework.caching;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.Mixins;
import org.qi4j.api.annotation.SideEffects;

/**
 * TODO
 */
@Mixins( InvocationCacheMixin.class )
@Assertions( ReturnCachedValueAssertion.class )
@SideEffects( { CacheInvocationResultSideEffect.class, InvalidateCacheOnSettersSideEffect.class } )
public interface InvocationCacheAbstractComposite
    extends Composite
{
}
