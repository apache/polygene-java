package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.ObjectMethodModel;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.composite.ParameterModel;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.property.PropertyModel;

/**
 * TODO
 */
public final class ObjectModelFactory extends AbstractModelFactory
{
    public ObjectModel newObjectModel( Class objectClass )
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorModel> constructorModels = new ArrayList<ConstructorModel>();
        getConstructorModels( objectClass, null, constructorModels );
        List<FieldModel> fieldModels = new ArrayList<FieldModel>();
        getFieldModels( objectClass, null, fieldModels );
        Iterable<MethodModel> methodModels = getMethodModels( objectClass );

        Iterable<ObjectMethodModel> objectMethodModels = getObjectMethodModels( objectClass );

        // Find properties
        List<PropertyModel> propertyModels = new ArrayList<PropertyModel>();
        for( ObjectMethodModel objectMethodModel : objectMethodModels )
        {
            if( objectMethodModel.getPropertyModel() != null )
            {
                propertyModels.add( objectMethodModel.getPropertyModel() );
            }
        }

        // Find associations
        List<AssociationModel> associationModels = new ArrayList<AssociationModel>();
        for( ObjectMethodModel objectMethodModel : objectMethodModels )
        {
            if( objectMethodModel.getAssociationModel() != null )
            {
                associationModels.add( objectMethodModel.getAssociationModel() );
            }
        }

        ObjectModel model = new ObjectModel( objectClass, constructorModels, fieldModels, methodModels, objectMethodModels, propertyModels, associationModels );
        return model;
    }

    private Iterable<ObjectMethodModel> getObjectMethodModels( Class objectClass )
    {
        List<ObjectMethodModel> objectMethodModels = new ArrayList<ObjectMethodModel>();
        for( Method method : objectClass.getMethods() )
        {
            // AbstractProperty model, if any
            PropertyModel propertyModel = null;
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                propertyModel = new PropertyModel( method );
            }

            // AbstractAssociation model, if any
            AssociationModel associationModel = null;
            if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                Type returnType = method.getGenericReturnType();
                Type associationType = ( (ParameterizedType) returnType ).getActualTypeArguments()[ 0 ];
                associationModel = new AssociationModel( associationType, method );
            }

            Type[] parameterTypes = method.getGenericParameterTypes();
            List<ParameterModel> parameters = new ArrayList<ParameterModel>();
            for( Type parameterType : parameterTypes )
            {
                ParameterModel parameterModel = new ParameterModel( parameterType );
                parameters.add( parameterModel );
            }
            ObjectMethodModel objectMethodModel = new ObjectMethodModel( method, parameters, propertyModel, associationModel );
            objectMethodModels.add( objectMethodModel );
        }

        return objectMethodModels;
    }
}