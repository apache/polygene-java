package org.qi4j.spi.injection;

import java.lang.reflect.InvocationHandler;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.State;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class MixinInjectionContext extends FragmentInjectionContext
    implements StateInjectionContext
{
    private State state;
    private Iterable<Object> adapt;
    private Object decorated;

    public MixinInjectionContext( StructureContext structureContext,
                                  ModuleBinding moduleBinding, CompositeBinding compositeBinding, InvocationHandler thisCompositeAs,
                                  Iterable<Object> adapt, Object decorated, State state )
    {
        super( structureContext, moduleBinding, compositeBinding, thisCompositeAs );
        this.state = state;
        this.adapt = adapt;
        this.decorated = decorated;
    }

    public State getState()
    {
        return state;
    }

    public Iterable<Object> getAdapt()
    {
        return adapt;
    }

    public Object getDecorated()
    {
        return decorated;
    }
}