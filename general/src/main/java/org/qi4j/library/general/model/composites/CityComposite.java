package org.qi4j.library.general.model.composites;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.persistence.Identity;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.Name;

/**
 * This interface represents Entity of a City
 */
@ImplementedBy( { PropertiesMixin.class } )
public interface CityComposite extends Name, Identity, Composite
{
}
