package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.model.ConcernModel;
import org.qi4j.model.ObjectModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class ConcernModelResolver
    extends ModifierModelResolver<ConcernResolution>
{
    public ConcernModelResolver( DependencyResolverDelegator dependencyResolverDelegator )
    {
        super( dependencyResolverDelegator );
    }

    @Override public <T extends ObjectModel> ConcernResolution resolveModel( ObjectModel<T> concernModel ) throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( concernModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( concernModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( concernModel.getMethodDependencies(), methodDependencies );

        ConcernResolution<T> model = new ConcernResolution<T>( (ConcernModel<T>) concernModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}