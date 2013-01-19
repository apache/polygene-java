package org.qi4j.manual.recipes.concern;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesToFilter;

// START SNIPPET: filter
public class MyAppliesToFilter implements AppliesToFilter
{
    public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
    {
        boolean appliesTo = evaluate(method); // Do whatever you want
        return appliesTo;
    }

// START SNIPPET: filter
    private boolean evaluate( Method method )
    {
        return true;
    }
// END SNIPPET: filter
}
// END SNIPPET: filter
