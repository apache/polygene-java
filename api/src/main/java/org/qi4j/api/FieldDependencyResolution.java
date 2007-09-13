package org.qi4j.api;

import java.lang.reflect.Field;
import org.qi4j.api.model.FieldDependency;

/**
 * TODO
 */
public class FieldDependencyResolution
{
    FieldDependency field;
    DependencyResolution depedencyResolution;

    public FieldDependencyResolution( FieldDependency field, DependencyResolution depedencyResolution )
    {
        this.field = field;
        this.depedencyResolution = depedencyResolution;
    }

    public FieldDependency getFieldDependency()
    {
        return field;
    }

    public DependencyResolution getDepedencyResolution()
    {
        return depedencyResolution;
    }
}
