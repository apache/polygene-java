package org.qi4j.library.general.model.composites;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.general.model.Country;
import org.qi4j.library.framework.properties.PropertiesMixin;

/**
 * This interface represents ValueObject of Country
 */
@ImplementedBy( { PropertiesMixin.class } )
public interface CountryComposite extends Country, Composite
{
}
