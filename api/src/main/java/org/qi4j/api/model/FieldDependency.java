package org.qi4j.api.model;

import java.lang.reflect.Field;

/**
 * TODO
 */
public final class FieldDependency
    extends Dependency
{
    private Field field;

    public FieldDependency( DependencyKey key, boolean optional, Field field )
    {
        super( key, optional );
        this.field = field;
    }

    public Field getField()
    {
        return field;
    }
}