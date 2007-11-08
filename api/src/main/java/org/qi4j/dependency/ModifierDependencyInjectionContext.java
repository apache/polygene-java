package org.qi4j.dependency;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.InvocationContext;
import org.qi4j.model.CompositeContext;
import org.qi4j.model.MixinModel;

/**
 * TODO
 */
public class ModifierDependencyInjectionContext
    extends DependencyInjectionContext
    implements FragmentDependencyInjectionContext
{
    private CompositeContext context;
    private InvocationHandler thisAs;
    private Object modifies;
    private Method method;
    private MixinModel model;
    private InvocationContext invocationContext;

    public ModifierDependencyInjectionContext( CompositeContext context, InvocationHandler thisAs, Object modifies, Method method, MixinModel model, InvocationContext invocationContext )
    {
        this.thisAs = thisAs;
        this.context = context;
        this.modifies = modifies;
        this.method = method;
        this.model = model;
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

    public MixinModel getModel()
    {
        return model;
    }

    public InvocationContext getInvocationContext()
    {
        return invocationContext;
    }

    public CompositeContext getContext()
    {
        return context;
    }

    public InvocationHandler getThisCompositeAs()
    {
        return thisAs;
    }
}