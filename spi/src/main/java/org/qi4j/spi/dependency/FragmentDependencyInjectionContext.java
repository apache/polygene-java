package org.qi4j.spi.dependency;

import org.qi4j.api.model.CompositeContext;

/**
 * TODO
 */
public interface FragmentDependencyInjectionContext
{
    public CompositeContext getContext();

    public Object getThisAs();
}