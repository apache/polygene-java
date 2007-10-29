package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.model.SideEffectModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class SideEffectModelResolver
    extends ModifierModelResolver
{
    public SideEffectModelResolver( DependencyResolverDelegator dependencyResolverDelegator )
    {
        super( dependencyResolverDelegator );
    }

    public <T> SideEffectResolution<T> resolveModel( SideEffectModel<T> sideEffectModel )
        throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( sideEffectModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( sideEffectModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( sideEffectModel.getMethodDependencies(), methodDependencies );

        SideEffectResolution<T> model = new SideEffectResolution<T>( sideEffectModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}
