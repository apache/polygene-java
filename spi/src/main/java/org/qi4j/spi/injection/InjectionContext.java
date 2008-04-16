package org.qi4j.spi.injection;

import org.qi4j.structure.Module;

/**
 * TODO
 */
public abstract class InjectionContext
{
    private StructureContext structureContext;
    private Module module;

    public InjectionContext( StructureContext structureContext,
                             Module module )
    {
        this.structureContext = structureContext;
        this.module = module;
    }

    public StructureContext getStructureContext()
    {
        return structureContext;
    }

    public Module getModule()
    {
        return module;
    }
}