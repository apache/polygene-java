package org.qi4j.spi.dependency;

import org.qi4j.CompositeBuilderFactory;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class ObjectInjectionContext
    extends InjectionContext
{
    private Iterable<Object> adapt;
    private Object decorated;

    public ObjectInjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding, Iterable<Object> adapt, Object decorated )
    {
        super( compositeBuilderFactory, objectBuilderFactory, moduleBinding );
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
}