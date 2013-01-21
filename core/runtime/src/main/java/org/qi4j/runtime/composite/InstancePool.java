package org.qi4j.runtime.composite;

/**
 * JAVADOC
 */
public interface InstancePool<T>
{
    public T obtainInstance();

    public void releaseInstance( T instance );
}
