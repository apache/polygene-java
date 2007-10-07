package org.qi4j.runtime.resolution;

import org.qi4j.api.model.SideEffectModel;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class SideEffectResolution<T>
    extends ModifierResolution<T>
{
    // Constructors --------------------------------------------------
    public SideEffectResolution( SideEffectModel<T> sideEffectModel, Iterable<ConstructorDependencyResolution> constructorDependencies, Iterable<FieldDependencyResolution> fieldDependencies, Iterable<MethodDependencyResolution> methodDependencies )
    {
        super( sideEffectModel, constructorDependencies, fieldDependencies, methodDependencies );
    }

    public SideEffectModel<T> getSideEffectModel()
    {
        return (SideEffectModel<T>) getObjectModel();
    }
}
