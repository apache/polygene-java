package org.qi4j.spi.entity;

import java.util.Iterator;

/**
 * Implementations of this is returned by EntityStore.iterator()
 * and allows the caller to ask for the next identity instead of the
 * entire state.
 */
public interface EntityIterator
    extends Iterator<EntityState>
{
    QualifiedIdentity nextIdentity();
}
