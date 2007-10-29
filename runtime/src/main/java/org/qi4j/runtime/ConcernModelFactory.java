package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ConcernModel;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.NullArgumentException;

/**
 * TODO
 */
public class ConcernModelFactory
    extends FragmentModelFactory<ConcernModel>
{
    public ConcernModelFactory()
    {
    }

    public <T> ConcernModel newFragmentModel( Class<T> modifierClass, Class compositeType, Class declaredBy ) throws NullArgumentException, InvalidCompositeException
    {
        modifierClass = getFragmentClass( modifierClass );

        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( modifierClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( modifierClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( modifierClass, compositeType, methodDependencies );

        Class[] appliesTo = getAppliesTo( modifierClass );

        ConcernModel<T> model = new ConcernModel<T>( modifierClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo, declaredBy );
        return model;
    }
}