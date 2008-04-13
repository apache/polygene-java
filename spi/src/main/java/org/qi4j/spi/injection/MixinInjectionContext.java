package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.composite.State;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class MixinInjectionContext extends FragmentInjectionContext
    implements StateInjectionContext
{
    private State state;
    private Iterable<Object> uses;

    public MixinInjectionContext( StructureContext structureContext,
                                  ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler This,
                                  Iterable<Object> uses, State state )
    {
        super( structureContext, moduleBinding, compositeBinding, This );
        this.state = state;
        this.uses = uses;
    }

    public State getState()
    {
        return state;
    }

    public Iterable<Object> getUses()
    {
        return uses;
    }
}