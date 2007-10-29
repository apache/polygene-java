package org.qi4j.query;

/**
 * TODO
 */
public interface QueryParameters
{
    void setParameter( String aName, Object aValue )
        throws IllegalArgumentException;
}