package org.qi4j.spi.injection;

import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.property.AbstractProperty;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
    implements PropertyInjectionContext
{
    private Iterable<Object> adapt;
    private Object decorated;
    private Map<String, AbstractProperty> properties;

    public ObjectInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, Iterable<Object> adapt, Object decorated, Map<String, AbstractProperty> properties )
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

    public Map<String, AbstractProperty> getProperties()
    {
        return properties;
    }
}