package org.qi4j.manual.recipes.createEntity;

// START SNIPPET: repo
public interface ManufacturerRepository
{
    Manufacturer findByIdentity(String identity);

    Manufacturer findByName(String name);
}
// END SNIPPET: repo
