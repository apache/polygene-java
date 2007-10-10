package org.qi4j.api.annotation;

import java.lang.reflect.Method;

/**
 * Implementations of this interface can be specified in the AppliesTo.
 * An instance of the provided class will be used to test if the modifier or mixin
 * should be applied to the method or not.
 */
public interface AppliesToFilter
{
    boolean appliesTo( Method method, Class mixin, Class compositeType );
}
