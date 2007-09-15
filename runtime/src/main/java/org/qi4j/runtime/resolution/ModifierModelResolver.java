package org.qi4j.runtime.resolution;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class ModifierModelResolver
    extends FragmentModelResolver
{
    public ModifierModelResolver( DependencyResolverDelegator dependencyResolverDelegator )
    {
        super( dependencyResolverDelegator );
    }

    public <T> ModifierResolution<T> resolveModifierModel( ModifierModel<T> modifierModel )
        throws InvalidDependencyException
    {

        List<ConstructorDependencyResolution> constructorDependencies = new ArrayList<ConstructorDependencyResolution>();
        resolveConstructorDependencies( modifierModel.getConstructorDependencies(), constructorDependencies );
        List<FieldDependencyResolution> fieldDependencies = new ArrayList<FieldDependencyResolution>();
        resolveFieldDependencies( modifierModel.getFieldDependencies(), fieldDependencies );
        List<MethodDependencyResolution> methodDependencies = new ArrayList<MethodDependencyResolution>();
        resolveMethodDependencies( modifierModel.getMethodDependencies(), methodDependencies );

        ModifierResolution<T> model = new ModifierResolution<T>( modifierModel, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}