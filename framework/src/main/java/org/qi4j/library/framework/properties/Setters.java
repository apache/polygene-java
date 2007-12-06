package org.qi4j.library.framework.properties;

import java.lang.reflect.Method;
import org.qi4j.composite.AppliesToFilter;

/**
 * Filter for setter methods. Method name must match "set*","add*" or "remove*".
 */
public class Setters
    implements AppliesToFilter
{
    public boolean appliesTo( Method method, Class mixin, Class compositeType, Class modelClass )
    {
        String name = method.getName();
        return name.startsWith( "set" ) ||
               name.startsWith( "add" ) ||
               name.startsWith( "remove" );
    }
}
