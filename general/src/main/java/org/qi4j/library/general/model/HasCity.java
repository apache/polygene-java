package org.qi4j.library.general.model;

import java.io.Serializable;
import org.qi4j.library.general.model.composites.CityComposite;

/**
 * Represents one-to-one relationship with {@link org.qi4j.library.general.model.composites.CityComposite}
 */
public interface HasCity extends Serializable
{
    void setCity( CityComposite city );

    CityComposite getCity();
}
