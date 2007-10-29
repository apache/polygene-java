package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ConcernModel;
import org.qi4j.api.model.ObjectModel;
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

    @Override public <T extends ObjectModel> ConcernResolution resolveModel( ObjectModel<T> assertionModel ) throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( assertionModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( assertionModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( assertionModel.getMethodDependencies(), methodDependencies );

        ConcernResolution<T> model = new ConcernResolution<T>( (ConcernModel<T>) assertionModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}