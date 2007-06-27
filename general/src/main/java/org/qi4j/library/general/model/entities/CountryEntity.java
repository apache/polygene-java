package org.qi4j.library.general.model.entities;

import org.qi4j.api.persistence.Identity;
import org.qi4j.library.general.model.Country;

/**
 * This interface represents entity of a Country whose values are stored in
 * {@link org.qi4j.library.general.model.Country} and is identifiable using
 * {@link org.qi4j.api.persistence.Identity}.
 */
public interface CountryEntity extends Country, Identity
{
}
