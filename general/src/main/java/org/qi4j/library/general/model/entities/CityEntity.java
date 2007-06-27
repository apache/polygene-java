package org.qi4j.library.general.model.entities;

import org.qi4j.api.persistence.Identity;
import org.qi4j.library.general.model.City;

/**
 * This interface represents entity of a City whose values are stored in
 * {@link org.qi4j.library.general.model.City} and is identifiable using
 * {@link org.qi4j.api.persistence.Identity}.
 */
public interface CityEntity extends City, Identity
{
}
