package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public class FragmentInjectionContext extends InjectionContext
{
    private CompositeBinding compositeBinding;
    private InvocationHandler This;

    public FragmentInjectionContext( StructureContext structureContext,
                                     Module module,
                                     CompositeBinding compositeBinding,
                                     InvocationHandler This )
    {
        super( structureContext, module );
        this.compositeBinding = compositeBinding;
        this.This = This;
    }

    public CompositeBinding getCompositeBinding()
    {
        return compositeBinding;
    }

    public InvocationHandler getThis()
    {
        return This;
    }
}