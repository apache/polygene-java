package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for Country.
 *
 * Country is a value object that stores ISO-CODE and the country name
 */
public interface Country extends Serializable
{
    String getIsoCode();

    void setIsoCode( String aIsoCode );

    String getCountryName();

    void setCountryName( String aName );
}
