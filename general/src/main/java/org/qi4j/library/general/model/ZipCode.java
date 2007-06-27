package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for zip code.
 * <p/>
 * Zipcode is a value object that stores the zip-code value itself.
 */
public interface ZipCode extends Serializable
{
    String getZipCode();

    void setZipCode( String aZipCode );
}
