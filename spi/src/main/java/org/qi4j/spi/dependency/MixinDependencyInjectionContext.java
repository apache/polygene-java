package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.qi4j.PropertyValue;
import org.qi4j.model.CompositeContext;
import org.qi4j.model.InjectionKey;

/**
 * TODO
 */
public class MixinDependencyInjectionContext
    extends ObjectDependencyInjectionContext
    implements FragmentDependencyInjectionContext
{
    private CompositeContext context;
    private InvocationHandler thisAs;

    public MixinDependencyInjectionContext( CompositeContext context, InvocationHandler thisAs, Map<InjectionKey, PropertyValue> properties, Map<InjectionKey, Object> adapt, Map<InjectionKey, Object> decorate )
    {
        super( properties, adapt, decorate );
        this.thisAs = thisAs;
        this.context = context;
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