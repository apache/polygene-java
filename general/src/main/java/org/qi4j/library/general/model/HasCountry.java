package org.qi4j.library.general.model;

import org.qi4j.library.general.model.composites.CountryComposite;
import java.io.Serializable;

/**
 * Represents one-to-one relationship with {@link org.qi4j.library.general.model.composites.CountryComposite}
 */
public interface HasCountry extends Serializable
{
    void setCountry( CountryComposite country );

    CountryComposite getCountry();
}
