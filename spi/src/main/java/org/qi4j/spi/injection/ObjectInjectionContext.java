package org.qi4j.spi.injection;

import org.qi4j.structure.Module;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
{
    private Iterable<Object> uses;

    public ObjectInjectionContext( StructureContext structureContext,
                                   Module module,
                                   ModuleBinding moduleBinding,
                                   Iterable<Object> uses )
    {
        super( structureContext, module, moduleBinding );
        this.uses = uses;
    }

    public Iterable<Object> getUses()
    {
        return uses;
    }
}