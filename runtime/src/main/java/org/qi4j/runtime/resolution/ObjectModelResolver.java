package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ObjectModel;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class ObjectModelResolver
    extends FragmentModelResolver
{
    public ObjectModelResolver( DependencyResolver dependencyResolver )
    {
        super( dependencyResolver );
    }

    public <T> ObjectResolution<T> resolveObjectModel( ObjectModel<T> objectModel )
        throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( objectModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( objectModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( objectModel.getMethodDependencies(), methodDependencies );

        ObjectResolution<T> model = new ObjectResolution<T>( objectModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}