package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.PropertyValue;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class MixinInjectionContext
    extends FragmentInjectionContext
    implements PropertyInjectionContext
{
    private Map<String, PropertyValue> properties;
    private Iterable<Object> adapt;
    private Object decorated;

    public MixinInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs, Map<String, PropertyValue> properties, Iterable<Object> adapt, Object decorated )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding, compositeBinding, thisCompositeAs );
        this.properties = properties;
        this.adapt = adapt;
        this.decorated = decorated;
    }

    public Map<String, PropertyValue> getProperties()
    {
        return properties;
    }

    public Iterable<Object> getAdapt()
    {
        return adapt;
    }

    public Object getDecorated()
    {
        return decorated;
    }
}