package org.qi4j.spi.injection;

import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.property.Property;
import org.qi4j.spi.service.ServiceRegistry;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
    implements PropertyInjectionContext
{
    private Iterable<Object> adapt;
    private Object decorated;
    private Map<String, Property> properties;

    public ObjectInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ServiceRegistry serviceRegistry, ModuleBinding moduleBinding, Iterable<Object> adapt, Object decorated, Map<String, Property> properties, Map<String, AbstractAssociation> objectAssociations )
    {
        super( compositeBuilderFactory, objectBuilderFactory, serviceRegistry, moduleBinding );
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

    public Map<String, Property> getProperties()
    {
        return properties;
    }
}