package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.model.ConstructorDependency;
import org.qi4j.model.FieldDependency;
import org.qi4j.model.InvalidCompositeException;
import org.qi4j.model.MethodDependency;
import org.qi4j.model.NullArgumentException;
import org.qi4j.model.ObjectModel;

/**
 * TODO
 */
public class ObjectModelFactory
    extends AbstractModelFactory
{
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