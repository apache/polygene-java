package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.model.MixinModel;
import org.qi4j.model.ObjectModel;

/**
 * TODO
 */
public class MixinModelResolver
    extends FragmentModelResolver<MixinResolution>
{
    public MixinModelResolver( DependencyResolverRegistryImpl dependencyResolverRegistry )
    {
        super( dependencyResolverRegistry );
    }


    @Override public <T extends ObjectModel> MixinResolution resolveModel( ObjectModel<T> mixinModel ) throws InvalidDependencyException
    {
        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( mixinModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( mixinModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( mixinModel.getMethodDependencies(), methodDependencies );

        MixinResolution model = new MixinResolution( (MixinModel) mixinModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}