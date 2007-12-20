package org.qi4j.runtime.composite;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.entity.property.PropertyModel;

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
        getConstructorModels( objectClass, null, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( objectClass, null, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( objectClass );

        // Find properties
        List<PropertyModel> propertyModels = new ArrayList<PropertyModel>(); // TODO

        ObjectModel model = new ObjectModel( objectClass, constructorModels, fieldModels, methodModels, propertyModels );
        return model;
    }
}