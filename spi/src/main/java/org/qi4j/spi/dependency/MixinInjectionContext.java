package org.qi4j.spi.dependency;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.PropertyValue;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class MixinInjectionContext
    extends FragmentInjectionContext
{
    private Map<PropertyResolution, PropertyValue> properties;
    private Iterable<Object> adapt;
    private Object decorated;

    public MixinInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs, Map<PropertyResolution, PropertyValue> properties, Iterable<Object> adapt, Object decorated )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding, compositeBinding, thisCompositeAs );
        this.properties = properties;
        this.adapt = adapt;
        this.decorated = decorated;
    }

    public Map<PropertyResolution, PropertyValue> getProperties()
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