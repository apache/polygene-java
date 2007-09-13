package org.qi4j.api.model;

import java.lang.reflect.Field;
import org.qi4j.api.DependencyKey;

/**
 * TODO
 */
public class FieldDependency
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