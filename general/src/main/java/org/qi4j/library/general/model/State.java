package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * Generic interface of State that stores the state name
 */
public interface State extends Serializable
{
    String getStateName();

    void setStateName( String name );
}