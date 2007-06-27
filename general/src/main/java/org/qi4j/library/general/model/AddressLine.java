package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for Address lines.
 *
 * Address is a value object that stores address line 1, 2 and 3.
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
