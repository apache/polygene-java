package org.qi4j.spi.injection;

import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

/**
 * TODO
 */
public final class InjectionResolution extends ResolutionContext
{
    private InjectionModel injectionModel;

    public InjectionResolution( InjectionModel injectionModel, ModuleModel module, LayerModel layer, ApplicationModel application )
    {
        super( module, layer, application );
        this.injectionModel = injectionModel;
    }

    public InjectionModel getInjectionModel()
    {
        return injectionModel;
    }
}
