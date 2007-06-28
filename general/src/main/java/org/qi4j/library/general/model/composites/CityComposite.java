package org.qi4j.library.general.model.composites;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.general.model.City;
import org.qi4j.library.framework.properties.PropertiesMixin;

/**
 * This interface represents a ValueObject of a City
 */
@ImplementedBy( { PropertiesMixin.class } )
public interface CityComposite extends City, Composite
{
}
