package org.qi4j.library.general.model;

/**
 * This is a stateful mixin for a person.
 *
 * In this case person is just a value object that consists of a name and a gender
 */
public interface Person extends PersonName, Gender
{
}
