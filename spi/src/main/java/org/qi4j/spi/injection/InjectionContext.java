package org.qi4j.spi.injection;

import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.spi.service.ServiceRegistry;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public abstract class InjectionContext
{
    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private ServiceRegistry serviceRegistry;
    private ModuleBinding moduleBinding;

    public InjectionContext( CompositeBuilderFactory compositeBuilderFactory, ObjectBuilderFactory objectBuilderFactory, ServiceRegistry serviceRegistry, ModuleBinding moduleBinding )
    {
        this.serviceRegistry = serviceRegistry;
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

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }
}