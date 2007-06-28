package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic service interface to obtain the formatted or decorated value
 */
public interface Descriptor extends Serializable
{
    String getDisplayValue();
}
