package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

// START SNIPPET: carFactoryService
@Mixins( { CarEntityFactoryMixin.class } )
public interface CarEntityFactoryService
        extends CarEntityFactory, ServiceComposite
{}
// END SNIPPET: carFactoryService
