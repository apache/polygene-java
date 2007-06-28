package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for Country that stores ISO-CODE and country's name
 */
public interface Country extends Serializable
{
    String getIsoCode();

    void setIsoCode( String aIsoCode );

    String getCountryName();

    void setCountryName( String aName );
}
