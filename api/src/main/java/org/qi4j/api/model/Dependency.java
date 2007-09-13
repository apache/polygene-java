package org.qi4j.api.model;

import org.qi4j.api.DependencyKey;

/**
 * TODO
 */
public abstract class Dependency
{
    private DependencyKey key;
    private boolean optional;

    public Dependency( DependencyKey key, boolean optional )
    {
        this.key = key;
        this.optional = optional;
    }

    public DependencyKey getKey()
    {
        return key;
    }

    public boolean isOptional()
    {
        return optional;
    }
}