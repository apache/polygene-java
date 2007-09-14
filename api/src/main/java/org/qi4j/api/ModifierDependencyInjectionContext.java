package org.qi4j.api;

import java.lang.reflect.Method;
import org.qi4j.api.model.CompositeContext;

/**
 * TODO
 */
public class ModifierDependencyInjectionContext
    extends DependencyInjectionContext
{
    private Object modifies;
    Method method;

    public ModifierDependencyInjectionContext( CompositeContext context, Object thisAs, Object modifies, Method method )
    {
        super( context, thisAs );
        this.modifies = modifies;
        this.method = method;
    }

    public Object getModifies()
    {
        return modifies;
    }

    public Method getMethod()
    {
        return method;
    }
}