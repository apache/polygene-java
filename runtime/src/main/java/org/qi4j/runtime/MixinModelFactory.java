package org.qi4j.runtime;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.scope.PropertyField;
import org.qi4j.api.annotation.scope.PropertyParameter;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.ParameterDependency;
import org.qi4j.api.model.PropertyModel;

/**
 * TODO
 */
public class MixinModelFactory
    extends FragmentModelFactory
{
    private ModifierModelFactory modifierModelFactory;

    public MixinModelFactory( ModifierModelFactory modifierModelFactory )
    {
        this.modifierModelFactory = modifierModelFactory;
    }

    public <T> MixinModel<T> getMixinModel( Class<T> mixinClass, Class compositeType )
        throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( mixinClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( mixinClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( mixinClass, compositeType, methodDependencies );

        List<PropertyModel> properties = getPropertyModels( constructorDependencies, fieldDependencies, methodDependencies );

        Class appliesTo = getAppliesTo( mixinClass );

        List<ModifierModel> modifiers = getModifiers( mixinClass, compositeType );

        MixinModel<T> model = new MixinModel<T>( mixinClass, constructorDependencies, fieldDependencies, methodDependencies, properties, appliesTo, modifiers );
        return model;
    }

    private List<PropertyModel> getPropertyModels( List<ConstructorDependency> constructorDependencies, List<FieldDependency> fieldDependencies, List<MethodDependency> methodDependencies )
    {
        List<PropertyModel> properties = new ArrayList<PropertyModel>();

        for( ConstructorDependency constructorDependency : constructorDependencies )
        {
            for( ParameterDependency parameterDependency : constructorDependency.getParameterDependencies() )
            {
                if( parameterDependency.getKey().getAnnotationType().equals( PropertyParameter.class ) )
                {
                    properties.add( new PropertyModel( parameterDependency.getKey().getName(), parameterDependency.getKey().getGenericType() ) );
                }
            }
        }

        for( MethodDependency methodDependency : methodDependencies )
        {
            for( ParameterDependency parameterDependency : methodDependency.getParameterDependencies() )
            {
                if( parameterDependency.getKey().getAnnotationType().equals( PropertyParameter.class ) )
                {
                    properties.add( new PropertyModel( parameterDependency.getKey().getName(), parameterDependency.getKey().getGenericType() ) );
                }
            }
        }

        for( FieldDependency fieldDependency : fieldDependencies )
        {
            if( fieldDependency.getKey().getAnnotationType().equals( PropertyField.class ) )
            {
                properties.add( new PropertyModel( fieldDependency.getKey().getName(), fieldDependency.getKey().getGenericType() ) );
            }
        }
        return properties;
    }

    private List<ModifierModel> getModifiers( Class<?> aClass, Class compositeType )
    {
        List<ModifierModel> modifiers = new ArrayList<ModifierModel>();
        ModifiedBy modifiedBy = aClass.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            for( Class modifier : modifiedBy.value() )
            {
                modifiers.add( modifierModelFactory.newModifierModel( modifier, compositeType ) );
            }
        }

        // Check superclass
        if( !aClass.isInterface() && aClass != Object.class )
        {
            List<ModifierModel> superModifiers = getModifiers( aClass.getSuperclass(), compositeType );
            modifiers.addAll( superModifiers );
        }

        return modifiers;
    }
}