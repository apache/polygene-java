package org.qi4j.library.beans.properties;

import java.lang.reflect.Method;
import org.qi4j.composite.AppliesToFilter;

/**
 * Filter for setter methods. Method name must match "set*","add*" or "remove*".
 */
public class Setters implements AppliesToFilter
{
    public static final MethodPrefixFilter SET = new MethodPrefixFilter( "set" );
    public static final MethodPrefixFilter ADD = new MethodPrefixFilter( "add" );
    public static final MethodPrefixFilter REMOVE = new MethodPrefixFilter( "remove" );
    public static final AppliesToFilter SETTERS = new OrAppliesToFilter( SET, ADD, REMOVE );

    public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
    {
        return SETTERS.appliesTo( method, mixin, compositeType, modelClass );
    }
}
