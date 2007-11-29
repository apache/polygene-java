package org.qi4j.spi.composite;

/**
 * Modifiers provide stateless modifications of methodModel invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public abstract class ModifierResolution
    extends FragmentResolution
{
    public ModifierResolution( ModifierModel modifierModel, Iterable<ConstructorResolution> constructorDependencies, Iterable<FieldResolution> fieldResolutions, Iterable<MethodResolution> methodResolutions )
    {
        super( modifierModel, constructorDependencies, fieldResolutions, methodResolutions );
    }
}
