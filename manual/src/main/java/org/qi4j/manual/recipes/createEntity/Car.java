package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

// START SNIPPET: entity
public interface Car
{
    @Immutable
    Association<Manufacturer> manufacturer();

    @Immutable
    Property<String> model();

    ManyAssociation<Accident> accidents();
}

// END SNIPPET: entity
