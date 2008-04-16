package org.qi4j.spi.injection;

import org.qi4j.structure.Module;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
{
    private Iterable<Object> uses;

    public ObjectInjectionContext( StructureContext structureContext,
                                   Module module,
                                   Iterable<Object> uses )
    {
        super( structureContext, module );
        this.uses = uses;
    }

    public Iterable<Object> getUses()
    {
        return uses;
    }
}