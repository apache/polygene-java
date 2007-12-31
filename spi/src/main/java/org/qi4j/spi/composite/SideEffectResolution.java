package org.qi4j.spi.composite;

/**
 * Modifiers provide stateless modifications of methodModel invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class SideEffectResolution extends ModifierResolution
{
    public SideEffectResolution( SideEffectModel sideEffectModel, Iterable<ConstructorResolution> constructorResolutions, Iterable<FieldResolution> fieldResolutions, Iterable<MethodResolution> methodResolutions )
    {
        super( sideEffectModel, constructorResolutions, fieldResolutions, methodResolutions );
    }

    public SideEffectModel getSideEffectModel()
    {
        return (SideEffectModel) getAbstractModel();
    }
}
