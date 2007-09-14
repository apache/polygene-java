package org.qi4j.api.model;

import org.qi4j.api.DependencyKey;

/**
 * TODO
 */
public final class ParameterDependency
    extends Dependency
{
    private String name;

    public ParameterDependency( DependencyKey key, boolean optional, String name )
    {
        super( key, optional );
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}