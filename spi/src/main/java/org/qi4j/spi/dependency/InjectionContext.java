package org.qi4j.spi.dependency;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public class InjectionContext
{
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private ModuleBinding moduleBinding;

    public InjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ModuleBinding moduleBinding )
    {
        this.compositeBuilderFactory = compositeBuilderFactory;
        this.objectBuilderFactory = objectBuilderFactory;
        this.moduleBinding = moduleBinding;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }
}