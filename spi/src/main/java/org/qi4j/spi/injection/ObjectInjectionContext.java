package org.qi4j.spi.injection;

import org.qi4j.spi.composite.State;
import org.qi4j.spi.structure.ModuleBinding;

/**
 * TODO
 */
public final class ObjectInjectionContext extends InjectionContext
    implements StateInjectionContext
{
    private Iterable<Object> adapt;
    private Object decorated;
    private State state;

    public ObjectInjectionContext( StructureContext structureContext,
                                   ModuleBinding moduleBinding,
                                   Iterable<Object> adapt,
                                   Object decorated,
                                   State state )
    {
        super( structureContext, moduleBinding );
        this.adapt = adapt;
        this.state = state;
        this.decorated = decorated;
    }

    public Iterable<Object> getAdapt()
    {
        return adapt;
    }

    public Object getDecorated()
    {
        return decorated;
    }

    public State getState()
    {
        return state;
    }
}