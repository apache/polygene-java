package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.Assertions;
import org.qi4j.api.annotation.SideEffects;
import org.qi4j.api.annotation.scope.PropertyField;
import org.qi4j.api.annotation.scope.PropertyParameter;
import org.qi4j.api.model.AssertionModel;
import org.qi4j.api.model.ConstructorDependency;
import org.qi4j.api.model.FieldDependency;
import org.qi4j.api.model.FragmentModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodDependency;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.ParameterDependency;
import org.qi4j.api.model.PropertyModel;
import org.qi4j.api.model.SideEffectModel;

/**
 * TODO
 */
public class MixinModelFactory
    extends FragmentModelFactory<MixinModel>
{
    private AssertionModelFactory assertionModelFactory;
    private SideEffectModelFactory sideEffectModelFactory;

    public MixinModelFactory( AssertionModelFactory assertionModelFactory, SideEffectModelFactory sideEffectModelFactory )
    {
        this.assertionModelFactory = assertionModelFactory;
        this.sideEffectModelFactory = sideEffectModelFactory;
    }

    public <T> MixinModel newFragmentModel( Class<T> mixinClass, Class compositeType ) throws NullArgumentException, InvalidCompositeException
    {
        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( mixinClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( mixinClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( mixinClass, compositeType, methodDependencies );

        List<PropertyModel> properties = getPropertyModels( constructorDependencies, fieldDependencies, methodDependencies );

        Class appliesTo = getAppliesTo( mixinClass );

        List<AssertionModel> assertions = getModifiers( mixinClass, compositeType, Assertions.class, assertionModelFactory );
        List<SideEffectModel> sideEffects = getModifiers( mixinClass, compositeType, SideEffects.class, sideEffectModelFactory );

        MixinModel<T> model = new MixinModel<T>( mixinClass, constructorDependencies, fieldDependencies, methodDependencies, properties, appliesTo, assertions, sideEffects );
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

    private <K extends FragmentModel> List<K> getModifiers( Class<?> aClass, Class compositeType, Class annotationClass, FragmentModelFactory<K> modelFactory )
    {
        List<K> modifiers = new ArrayList<K>();
        Annotation modifierAnnotation = aClass.getAnnotation( annotationClass );
        if( modifierAnnotation != null )
        {
            Class[] modifierClasses = null;
            try
            {
                modifierClasses = (Class[]) annotationClass.getMethod( "value" ).invoke( modifierAnnotation );
            }
            catch( Exception e )
            {
                // Should not happen
                e.printStackTrace();
            }
            for( Class modifier : modifierClasses )
            {
                K assertionModel = (K) modelFactory.newFragmentModel( modifier, compositeType );
                modifiers.add( assertionModel );
            }
        }

        // Check superclass
        if( !aClass.isInterface() && aClass != Object.class )
        {
            List<K> superAssertions = getModifiers( aClass.getSuperclass(), compositeType, annotationClass, modelFactory );
            modifiers.addAll( superAssertions );
        }

        return modifiers;
    }
}