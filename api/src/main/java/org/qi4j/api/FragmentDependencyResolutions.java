package org.qi4j.api;

/**
 * TODO
 */
public class FragmentDependencyResolutions
{
    private ConstructorDependencyResolution constructorDependencyResolution;
    private Iterable<FieldDependencyResolution> fieldDependencyResolutions;

    public FragmentDependencyResolutions( ConstructorDependencyResolution constructorDependencyResolution, Iterable<FieldDependencyResolution> fieldDependencyResolutions )
    {
        this.constructorDependencyResolution = constructorDependencyResolution;
        this.fieldDependencyResolutions = fieldDependencyResolutions;
    }

    public ConstructorDependencyResolution getConstructorDependencyResolution()
    {
        return constructorDependencyResolution;
    }

    public Iterable<FieldDependencyResolution> getFieldDependencyResolutions()
    {
        return fieldDependencyResolutions;
    }
}
