package org.qi4j.api;

import org.qi4j.api.model.CompositeContext;

/**
 * TODO
 */
public class DependencyInjectionContext
{
    CompositeContext context;
    Object thisAs;

    public DependencyInjectionContext( CompositeContext context, Object thisAs )
    {
        this.context = context;
        this.thisAs = thisAs;
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
