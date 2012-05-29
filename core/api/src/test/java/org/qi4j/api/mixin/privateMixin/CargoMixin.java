package org.qi4j.api.mixin.privateMixin;

import org.qi4j.api.injection.scope.This;

// START SNIPPET: private
public abstract class CargoMixin
        implements Cargo
{
    @This
    private CargoState state;

    public String origin()
    {
        return state.origin().get();
    }

    public String destination()
    {
        return state.destination().get();
    }

    public void changeDestination( String newDestination )
    {
        state.destination().set( newDestination );
    }
}

// END SNIPPET: private