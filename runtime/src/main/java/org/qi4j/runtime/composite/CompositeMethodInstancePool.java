package org.qi4j.runtime.composite;

/**
 * JAVADOC
 */
public interface CompositeMethodInstancePool
{
    public CompositeMethodInstance getInstance();

    public void returnInstance( CompositeMethodInstance instance );
}
