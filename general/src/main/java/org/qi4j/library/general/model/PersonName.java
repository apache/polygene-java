package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for person's name.
 * <p/>
 * PersonName is a value object that stores first and last name.
 */
public interface PersonName extends Serializable
{
    String getFirstName();

    void setFirstName( String aName );

    String getLastName();

    void setLastName( String aName );
}
