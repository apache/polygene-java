package org.qi4j.functional;

/**
 * Interface that visitable objects should implement.
 */
public interface Visitable<T>
{
    <ThrowableType extends Throwable> boolean accept( Visitor<? super T, ThrowableType> visitor )
        throws ThrowableType;
}
