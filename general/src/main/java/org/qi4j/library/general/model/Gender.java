package org.qi4j.library.general.model;

import java.io.Serializable;

/**
 * This is a stateful mixin for Gender.
 * 
 * Gender is a value object that stores a gender type
 */
public interface Gender extends Serializable
{
    GenderType getGender();

    void setGender(GenderType gender);
}
