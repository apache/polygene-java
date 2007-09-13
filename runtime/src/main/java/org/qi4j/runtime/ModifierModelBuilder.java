package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.Dependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.ParameterDependency;

/**
 * TODO
 */
public class ModifierModelBuilder
    extends FragmentModelBuilder
{
    public ModifierModelBuilder()
    {
    }

    public <T> ModifierModel<T> getModifierModel( Class<T> modifierClass, Class compositeType)
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>( );
        getConstructorDependencies( modifierClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>( );
        getFieldDependencies( modifierClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>( );
        getMethodDependencies( modifierClass, compositeType, methodDependencies );

        Class appliesTo = getAppliesTo( modifierClass );

        ModifierModel<T> model = new ModifierModel<T>( modifierClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo);
        return model;

    }
}