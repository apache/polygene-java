package org.qi4j.entitystore.neo4j;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.entitystore.EntityStateVersions;
import org.qi4j.spi.entitystore.EntityStore;

@Mixins( NeoEntityStoreMixin.class )
public interface NeoEntityStoreService
    extends EntityStore, ServiceComposite, EntityStateVersions, ServiceActivation
{
}
