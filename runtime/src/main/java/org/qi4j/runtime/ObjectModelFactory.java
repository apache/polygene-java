package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.ObjectModel;

/**
 * TODO
 */
public class ObjectModelFactory
    extends FragmentModelFactory
{
    public ObjectModelFactory()
    {
    }

    public <T> ObjectModel<T> newObjectModel( Class<T> objectClass )
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( objectClass, null, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( objectClass, null, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( objectClass, null, methodDependencies );

        ObjectModel<T> model = new ObjectModel<T>( objectClass, constructorDependencies, fieldDependencies, methodDependencies );
        return model;
    }
}