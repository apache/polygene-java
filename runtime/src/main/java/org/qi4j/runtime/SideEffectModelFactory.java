package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.SideEffectModel;

/**
 * TODO
 */
public class SideEffectModelFactory
    extends FragmentModelFactory<SideEffectModel>
{
    public SideEffectModelFactory()
    {
    }

    public <T> SideEffectModel newFragmentModel( Class<T> modifierClass, Class compositeType, Class declaredBy ) throws NullArgumentException, InvalidCompositeException
    {
        modifierClass = getFragmentClass( modifierClass );

        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( modifierClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( modifierClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( modifierClass, compositeType, methodDependencies );

        Class[] appliesTo = getAppliesTo( modifierClass );

        SideEffectModel<T> model = new SideEffectModel<T>( modifierClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo, declaredBy );
        return model;

    }
}
