package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.entity.EntityComposite;

// START SNIPPET: composite
public interface CarEntity extends Car, EntityComposite
{}

// END SNIPPET: composite