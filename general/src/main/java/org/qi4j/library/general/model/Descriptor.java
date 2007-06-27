package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateless mixin to obtain the formatted or decorated value
 */
public interface Descriptor extends Serializable
{
    String getDisplayValue();
}
