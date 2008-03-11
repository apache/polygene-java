package org.qi4j.spi.injection;

import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public abstract class InjectionContext
{
    private StructureContext structureContext;
    private ModuleBinding moduleBinding;

    public InjectionContext( StructureContext structureContext,
                             ModuleBinding moduleBinding )
    {
        this.structureContext = structureContext;
        this.moduleBinding = moduleBinding;
    }

    public StructureContext getStructureContext()
    {
        return structureContext;
    }

    public ModuleBinding getModuleBinding()
    {
        return moduleBinding;
    }
}