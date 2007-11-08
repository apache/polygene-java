package org.qi4j.runtime.resolution;

import org.qi4j.dependency.DependencyResolution;
import org.qi4j.model.FieldDependency;

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

    public DependencyResolution getDependencyResolution()
    {
        return depedencyResolution;
    }
}
