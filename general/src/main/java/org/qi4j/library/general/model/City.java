package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for City.
 */
public interface City extends Serializable
{
    String getCityName();

    void setCityName( String aName );
}
