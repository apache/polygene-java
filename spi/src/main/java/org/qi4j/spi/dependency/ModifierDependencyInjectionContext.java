package org.qi4j.spi.dependency;

import java.lang.reflect.Method;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.model.CompositeContext;

/**
 * TODO
 */
public class ModifierDependencyInjectionContext
    extends DependencyInjectionContext
    implements FragmentDependencyInjectionContext
{
    private CompositeContext context;
    private Object thisAs;
    private Object modifies;
    private Method method;
    private InvocationContext invocationContext;

    public ModifierDependencyInjectionContext( CompositeContext context, Object thisAs, Object modifies, Method method, InvocationContext invocationContext )
    {
        this.thisAs = thisAs;
        this.context = context;
        this.modifies = modifies;
        this.method = method;
        this.invocationContext = invocationContext;
    }

    public Object getModifies()
    {
        return modifies;
    }

    public Method getMethod()
    {
        return method;
    }

    public InvocationContext getInvocationContext()
    {
        return invocationContext;
    }

    public CompositeContext getContext()
    {
        return context;
    }

    public Object getThisAs()
    {
        return thisAs;
    }
}