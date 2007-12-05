package org.qi4j.spi.dependency;

import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.PropertyValue;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class ObjectInjectionContext
    extends InjectionContext
    implements PropertyInjectionContext
{
    private Iterable<Object> adapt;
    private Object decorated;
    private Map<String, PropertyValue> properties;

    public ObjectInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, Iterable<Object> adapt, Object decorated, Map<String, PropertyValue> properties )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding );
        this.properties = properties;
        this.adapt = adapt;
        this.decorated = decorated;
    }

    public Iterable<Object> getAdapt()
    {
        return adapt;
    }

    public Object getDecorated()
    {
        return decorated;
    }

    public Map<String, PropertyValue> getProperties()
    {
        return properties;
    }
}