package org.qi4j.api.persistence;

/**
 * TODO
 */
public interface QueryParameters
{
    void setParameter( String aName, Object aValue )
        throws IllegalArgumentException;
}
