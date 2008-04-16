package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.composite.State;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.structure.Module;

/**
 * TODO
 */
public final class MixinInjectionContext extends FragmentInjectionContext
    implements StateInjectionContext
{
    private State state;
    private Iterable<Object> uses;

    public MixinInjectionContext( StructureContext structureContext,
                                  Module module, CompositeBinding compositeBinding, InvocationHandler This,
                                  Iterable<Object> uses, State state )
    {
        super( structureContext, module, compositeBinding, This );
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