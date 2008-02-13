package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.service.ServiceRegistry;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class FragmentInjectionContext extends InjectionContext
{
    private CompositeBinding compositeBinding;
    private InvocationHandler thisCompositeAs;

    public FragmentInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ServiceRegistry serviceRegistry, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs )
    {
        super( compositeBuilderFactory, objectBuilderFactory, serviceRegistry, moduleBinding );
        this.compositeBinding = compositeBinding;
        this.thisCompositeAs = thisCompositeAs;
    }

    public CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public InvocationHandler getThisCompositeAs()
    {
        return thisCompositeAs;
    }
}