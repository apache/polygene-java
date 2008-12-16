package org.qi4j.entity.rmi;

import java.rmi.registry.Registry;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

/**
 * RMI Registry service
 */
@Mixins( RegistryMixin.class )
public interface RegistryService
    extends ServiceComposite, Registry, Activatable
{
}
