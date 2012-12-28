package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.entity.EntityComposite;

// START SNIPPET: composite
public interface ManufacturerEntity extends Manufacturer, EntityComposite
{}

// END SNIPPET: composite