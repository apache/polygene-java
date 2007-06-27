package org.qi4j.library.general.model.entities;

import org.qi4j.api.persistence.Identity;
import org.qi4j.library.general.model.AddressLine;
import org.qi4j.library.general.model.ZipCode;

/**
 * This is an entity for Address.
 */
public interface AddressEntity extends AddressLine, ZipCode, CityEntity, StateEntity, CountryEntity, Identity
{
}
