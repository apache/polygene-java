package org.qi4j.library.general.model;

import org.qi4j.library.general.model.composites.StateComposite;
import java.io.Serializable;

/**
 * Represents one-to-one relationship with {@link org.qi4j.library.general.model.composites.StateComposite}
 */
public interface HasState extends Serializable
{
    void setState( StateComposite state );

    StateComposite getState();
}
