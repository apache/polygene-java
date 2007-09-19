package org.qi4j.api.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Dependency key used for dependency resolutions. The key is comprised
 * of the thisAs type, the fragment type, the annotation type, the generic dependency type, and an optional name.
 */
public class FragmentDependencyKey
    extends DependencyKey
{
    private Class compositeType;

    public FragmentDependencyKey( Class<? extends Annotation> annotationType, Type genericType, String name, Class dependentType, Class compositeType )
    {
        super( annotationType, genericType, name, dependentType );
        this.compositeType = compositeType;
    }

    public Class getCompositeType()
    {
        return compositeType;
    }

    @Override public String toString()
    {
        return compositeType.getSimpleName() + ":" + super.toString();
    }
}