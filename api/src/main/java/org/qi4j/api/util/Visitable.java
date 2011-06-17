package org.qi4j.api.util;

/**
 * Interface that visitable objects should implement.
 */
public interface Visitable<T>
{
    <ThrowableType extends Throwable> boolean accept(Visitor<? super T, ThrowableType> visitor)
        throws ThrowableType;
}
