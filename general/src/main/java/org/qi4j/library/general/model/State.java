package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for State.
 * <p/>
 * State is a value object that stores the state name
 */
public interface State extends Serializable
{
    String getStateName();

    void setStateName( String name );
}