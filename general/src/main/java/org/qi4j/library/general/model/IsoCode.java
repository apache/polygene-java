package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface for ISO-CODE.
 */
public interface IsoCode extends Serializable
{
    void setIsoCode( String isoCode );

    String getIsoCode();
}
