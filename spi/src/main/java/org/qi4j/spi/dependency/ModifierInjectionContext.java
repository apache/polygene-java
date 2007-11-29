package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.InvocationContext;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class ModifierInjectionContext
    extends FragmentInjectionContext
{
    private Object modifies;
    private Method method;
    private MixinBinding mixinBinding;
    private InvocationContext invocationContext;

    public ModifierInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs, Object modifies, Method method, MixinBinding mixinBinding, InvocationContext invocationContext )
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

    public Method getMethod()
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