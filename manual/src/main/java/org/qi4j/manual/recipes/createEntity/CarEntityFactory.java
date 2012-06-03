package org.qi4j.manual.recipes.createEntity;

// START SNIPPET: carFactory
public interface CarEntityFactory
{
    Car create(Manufacturer manufacturer, String model);
}

// END SNIPPET: carFactory