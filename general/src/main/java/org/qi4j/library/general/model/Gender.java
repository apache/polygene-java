package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for Gender that stores {@link GenderType}.
 */
public interface Gender extends Serializable
{
    GenderType getGender();

    void setGender(GenderType gender);
}
