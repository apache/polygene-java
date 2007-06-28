package org.qi4j.library.general.model.composites;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.AddressLine;
import org.qi4j.library.general.model.HasCity;
import org.qi4j.library.general.model.HasCountry;
import org.qi4j.library.general.model.HasState;
import org.qi4j.library.general.model.ZipCode;

/**
 * AddressComposite here is a ValueObject. An entity of AddressComposite would need to have an identity.
 */
@ImplementedBy( { PropertiesMixin.class } )
public interface AddressComposite extends AddressLine, ZipCode, HasCity, HasState, HasCountry, Composite
{
}
