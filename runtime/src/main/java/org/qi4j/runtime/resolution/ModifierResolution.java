package org.qi4j.runtime.resolution;

import org.qi4j.model.ModifierModel;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public abstract class ModifierResolution<T>
    extends FragmentResolution<T>
{
    // Constructors --------------------------------------------------
    public ModifierResolution( ModifierModel<T> modifierModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies )
    {
        super( modifierModel, constructorDependencies, fieldDependencies, methodDependencies );
    }
}
