package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.ObjectModel;

/**
 * TODO
 */
public class ObjectModelFactory
    extends AbstractModelFactory
{
    public ObjectModel newObjectModel( Class objectClass )
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorModel> constructorModels = new ArrayList<ConstructorModel>();
        getConstructorModels( objectClass, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( objectClass, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( objectClass );

        ObjectModel model = new ObjectModel( objectClass, constructorModels, fieldModels, methodModels );
        return model;
    }
}