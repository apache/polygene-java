package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.MixinBinding;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public final class ModifierInjectionContext extends FragmentInjectionContext
{
    private Object modifies;
    private CompositeMethodBinding method;
    private MixinBinding mixinBinding;

    public ModifierInjectionContext( StructureContext structureContext,
                                     Module module,
                                     CompositeBinding compositeBinding,
                                     InvocationHandler This,
                                     Object modifies,
                                     CompositeMethodBinding method,
                                     MixinBinding mixinBinding
    )
    {
        super( structureContext, module, compositeBinding, This );
        this.modifies = modifies;
        this.method = method;
        this.mixinBinding = mixinBinding;
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
}