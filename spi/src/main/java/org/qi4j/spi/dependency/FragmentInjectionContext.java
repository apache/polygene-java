package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class FragmentInjectionContext
    extends InjectionContext
{
    private CompositeBinding compositeBinding;
    private InvocationHandler thisCompositeAs;

    public FragmentInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding );
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