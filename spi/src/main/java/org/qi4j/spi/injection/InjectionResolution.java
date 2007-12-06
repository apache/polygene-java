package org.qi4j.spi.injection;

import org.qi4j.spi.composite.AbstractModel;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

/**
 * TODO
 */
public class InjectionResolution
    extends ResolutionContext
{
    private InjectionModel injectionModel;

    public InjectionResolution( InjectionModel injectionModel, AbstractModel objectModel, CompositeModel compositeModel, ModuleModel module, LayerModel layer, ApplicationModel application )
    {
        super( objectModel, compositeModel, module, layer, application );
        this.injectionModel = injectionModel;
    }

    public InjectionModel getInjectionModel()
    {
        return injectionModel;
    }
}
