package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import org.qi4j.model.CompositeContext;

/**
 * TODO
 */
public interface FragmentDependencyInjectionContext
{
    public CompositeContext getContext();

    public InvocationHandler getThisCompositeAs();
}