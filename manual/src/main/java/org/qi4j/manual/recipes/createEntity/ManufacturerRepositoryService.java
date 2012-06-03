package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

@Mixins( ManufacturerRepositoryMixin.class  )
public interface ManufacturerRepositoryService
        extends ManufacturerRepository, ServiceComposite
{}
