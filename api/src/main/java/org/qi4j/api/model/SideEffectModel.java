package org.qi4j.api.model;

import java.lang.annotation.Annotation;
import org.qi4j.api.annotation.scope.SideEffectFor;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class SideEffectModel<T>
    extends ModifierModel<T>
{
    public SideEffectModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class appliesTo )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo );
    }

    public Class<? extends Annotation> getModifiesAnnotationType()
    {
        return SideEffectFor.class;
    }
}
