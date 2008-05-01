package org.qi4j.spi.injection;

import org.qi4j.structure.Module;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public abstract class InjectionContext
{
    private StructureContext structureContext;
    private Module module;
    private ModuleBinding moduleBinding;

    public InjectionContext( StructureContext structureContext, Module module, ModuleBinding moduleBinding )
    {
        this.structureContext = structureContext;
        this.module = module;
        this.moduleBinding = moduleBinding;
    }

    public StructureContext getStructureContext()
    {
        return structureContext;
    }

    public Module getModule()
    {
        return module;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }
}