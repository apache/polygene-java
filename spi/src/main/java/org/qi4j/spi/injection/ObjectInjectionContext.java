package org.qi4j.spi.injection;

import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
{
    private Iterable<Object> uses;

    public ObjectInjectionContext( StructureContext structureContext,
                                   ModuleBinding moduleBinding,
                                   Iterable<Object> uses )
    {
        super( structureContext, moduleBinding );
        this.uses = uses;
    }

    public Iterable<Object> getUses()
    {
        return uses;
    }
}