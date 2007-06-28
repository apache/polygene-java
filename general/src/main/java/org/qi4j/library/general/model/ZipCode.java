package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for Zipcode that stores the zip-code value itself.
 */
public interface ZipCode extends Serializable
{
    String getZipCode();

    void setZipCode( String aZipCode );
}
