package org.qi4j.spi.module;

import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.spi.entitystore.EntityStore;

public interface ModuleSpi extends Module
{
    EntityStore entityStore();

    IdentityGenerator identityGenerator();

    ValueSerialization valueSerialization();

    Iterable<ModelModule<EntityDescriptor>> findVisibleEntityTypes();

    Iterable<ModelModule<ValueDescriptor>> findVisibleValueTypes();

    Iterable<ModelModule<TransientDescriptor>> findVisibleTransientTypes();

    Iterable<ModelModule<ObjectDescriptor>> findVisibleObjectTypes();

    Iterable<ModelModule<ServiceDescriptor>> findVisibleServiceTypes();
}
