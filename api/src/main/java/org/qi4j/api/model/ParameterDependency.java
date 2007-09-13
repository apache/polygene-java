package org.qi4j.api.model;

import java.lang.reflect.Field;
import org.qi4j.api.DependencyKey;

/**
 * TODO
 */
public class ParameterDependency
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