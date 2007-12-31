package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.property.AbstractProperty;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class MixinInjectionContext extends FragmentInjectionContext
    implements PropertyInjectionContext, AssociationInjectionContext
{
    private Map<String, AbstractProperty> properties;
    private Map<String, AbstractAssociation> associations;
    private Iterable<Object> adapt;
    private Object decorated;

    public MixinInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs, Iterable<Object> adapt, Object decorated, Map<String, AbstractProperty> properties, Map<String, AbstractAssociation> associations )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding, compositeBinding, thisCompositeAs );
        this.associations = associations;
        this.properties = properties;
        this.adapt = adapt;
        this.decorated = decorated;
    }

    public Map<String, AbstractProperty> getProperties()
    {
        return properties;
    }

    public Map<String, AbstractAssociation> getAssociations()
    {
        return associations;
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