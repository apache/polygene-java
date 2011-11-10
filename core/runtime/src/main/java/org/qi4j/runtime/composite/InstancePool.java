package org.qi4j.runtime.composite;

/**
 * JAVADOC
 */
public interface InstancePool<T>
{
    public T getInstance();

    public void returnInstance( T instance );
}
