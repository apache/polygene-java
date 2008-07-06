package org.qi4j.library.requirements;

public interface Requirement<T>
{
    boolean isSatisfied( T data );
}
