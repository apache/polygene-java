package org.qi4j.library.beans.properties;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesToFilter;

/**
 * Filter for getter methods. Method name must match "get*" or "is*" or "has*".
 */
public class Getters implements AppliesToFilter
{
    public static final MethodPrefixFilter GET = new MethodPrefixFilter( "get" );
    public static final MethodPrefixFilter IS = new MethodPrefixFilter( "is" );
    public static final MethodPrefixFilter HAS = new MethodPrefixFilter( "has" );
    public static final AppliesToFilter GETTERS = new OrAppliesToFilter( GET, IS, HAS );

    public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
    {
        return GETTERS.appliesTo( method, mixin, compositeType, modelClass );
    }
}
