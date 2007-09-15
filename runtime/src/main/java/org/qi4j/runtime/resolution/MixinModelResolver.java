package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.MixinModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class MixinModelResolver
    extends FragmentModelResolver
{
    public MixinModelResolver( DependencyResolverDelegator dependencyResolverDelegator )
    {
        super( dependencyResolverDelegator );
    }

    public <T> MixinResolution<T> resolveMixinModel( MixinModel<T> mixinModel )
        throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( mixinModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( mixinModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( mixinModel.getMethodDependencies(), methodDependencies );

        MixinResolution<T> model = new MixinResolution<T>( mixinModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}