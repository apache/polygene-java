package org.qi4j.spi.dependency;

import java.util.Map;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.InjectionKey;

/**
 * TODO
 */
public class MixinDependencyInjectionContext
    extends ObjectDependencyInjectionContext
    implements FragmentDependencyInjectionContext
{
    private CompositeContext context;
    private Object thisAs;

    public MixinDependencyInjectionContext( CompositeContext context, Object thisAs, Map<InjectionKey, Object> properties, Map<InjectionKey, Object> adapt, Map<InjectionKey, Object> decorate )
    {
        super( properties, adapt, decorate );
        this.thisAs = thisAs;
        this.context = context;
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