package org.qi4j.entitystore.qrm;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreSPI;

/**
 * User: alex
 */
@Mixins( { QrmEntityStoreServiceMixin.class } )
public interface QrmEntityStoreService
    extends ServiceComposite, Activatable, EntityStore, EntityStoreSPI
{
}
