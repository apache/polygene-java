package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvocationContext;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class ModifierInjectionContext
    extends FragmentInjectionContext
{
    private Object modifies;
    private CompositeMethodBinding method;
    private MixinBinding mixinBinding;
    private InvocationContext invocationContext;

    public ModifierInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs, Object modifies, CompositeMethodBinding method, MixinBinding mixinBinding, InvocationContext invocationContext )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding, compositeBinding, thisCompositeAs );
        this.modifies = modifies;
        this.method = method;
        this.mixinBinding = mixinBinding;
        this.invocationContext = invocationContext;
    }

    public Object getModifies()
    {
        return modifies;
    }

    public CompositeMethodBinding getMethod()
    {
        return method;
    }

    public MixinBinding getMixinBinding()
    {
        return mixinBinding;
    }

    public InvocationContext getInvocationContext()
    {
        return invocationContext;
    }
}