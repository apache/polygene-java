package org.apache.zest.api.identity;

/**
 * Representation of an Identity.
 * Identity is an opaque, immutable data type.
 * Identity is a very central concept in any domain model.
 *
 */
public interface Identity
{
    String toString();

    byte[] toBytes();
}
