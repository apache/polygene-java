package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for Address lines.
 */
public interface AddressLine extends Serializable
{
    void setFirstLine( String firstLine );

    String getFirstLine();

    void setSecondLine( String secondLine );

    String getSecondLine();

    void setThirdLine( String thirdLine );

    String getThirdLine();
}
