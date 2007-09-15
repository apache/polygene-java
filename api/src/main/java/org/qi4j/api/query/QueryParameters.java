package org.qi4j.api.query;

/**
 * TODO
 */
public interface QueryParameters
{
    void setParameter( String aName, Object aValue )
        throws IllegalArgumentException;
}