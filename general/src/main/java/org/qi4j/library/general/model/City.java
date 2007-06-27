package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for City.
 *
 * This interface represents value object of a City which stores a city name.
 */
public interface City extends Serializable
{
    String getCityName();

    void setCityName( String aName );
}
