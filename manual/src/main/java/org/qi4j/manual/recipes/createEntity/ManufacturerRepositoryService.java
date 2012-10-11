package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

// START SNIPPET: manufacturerRepositoryService
@Mixins( ManufacturerRepositoryMixin.class  )
public interface ManufacturerRepositoryService
        extends ManufacturerRepository, ServiceComposite
{}
// END SNIPPET: manufacturerRepositoryService
