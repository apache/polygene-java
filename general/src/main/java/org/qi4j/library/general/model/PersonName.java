package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface of PersonName that stores first and last name.
 */
public interface PersonName extends Serializable
{
    String getFirstName();

    void setFirstName( String aName );

    String getLastName();

    void setLastName( String aName );
}
