package org.qi4j.entity.jdbm;

import org.qi4j.composite.Mixins;
import org.qi4j.library.framework.locking.LockingAbstractComposite;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceComposite;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.serialization.SerializedEntityStoreMixin;

/**
 * EntityStore service backed by JDBM store.
 */

@Mixins( { SerializedEntityStoreMixin.class, JdbmSerializationStoreMixin.class } )
public interface JdbmEntityStoreComposite
    extends EntityStore, ServiceComposite, Activatable, LockingAbstractComposite

{
}
