package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.annotation.Concerns;
import org.qi4j.annotation.SideEffects;
import org.qi4j.annotation.scope.PropertyField;
import org.qi4j.annotation.scope.PropertyParameter;
import org.qi4j.model.ConcernModel;
import org.qi4j.model.ConstraintDeclarationModel;
import org.qi4j.model.ConstructorDependency;
import org.qi4j.model.FieldDependency;
import org.qi4j.model.FragmentModel;
import org.qi4j.model.InvalidCompositeException;
import org.qi4j.model.MethodDependency;
import org.qi4j.model.MixinModel;
import org.qi4j.model.NullArgumentException;
import org.qi4j.model.ParameterDependency;
import org.qi4j.model.PropertyModel;
import org.qi4j.model.SideEffectModel;

/**
 * TODO
 */
public class MixinModelFactory
    extends FragmentModelFactory<MixinModel>
{
    private ConcernModelFactory concernModelFactory;
    private SideEffectModelFactory sideEffectModelFactory;

    public MixinModelFactory( ConcernModelFactory concernModelFactory, SideEffectModelFactory sideEffectModelFactory )
    {
        this.concernModelFactory = concernModelFactory;
        this.sideEffectModelFactory = sideEffectModelFactory;
    }

    public <T> MixinModel newFragmentModel( Class<T> mixinClass, Class compositeType, Class declaredBy ) throws NullArgumentException, InvalidCompositeException
    {
        mixinClass = getFragmentClass( mixinClass );

        List<ConstructorDependency> constructorDependencies = new ArrayList<ConstructorDependency>();
        getConstructorDependencies( mixinClass, compositeType, constructorDependencies );
        List<FieldDependency> fieldDependencies = new ArrayList<FieldDependency>();
        getFieldDependencies( mixinClass, compositeType, fieldDependencies );
        List<MethodDependency> methodDependencies = new ArrayList<MethodDependency>();
        getMethodDependencies( mixinClass, compositeType, methodDependencies );

        List<PropertyModel> properties = getPropertyModels( constructorDependencies, fieldDependencies, methodDependencies );

        Class[] appliesTo = getAppliesTo( mixinClass );

        List<ConstraintDeclarationModel> constraints = Collections.emptyList(); // TODO
        List<ConcernModel> concerns = getModifiers( mixinClass, compositeType, Concerns.class, concernModelFactory );
        List<SideEffectModel> sideEffects = getModifiers( mixinClass, compositeType, SideEffects.class, sideEffectModelFactory );

        MixinModel<T> model = new MixinModel<T>( mixinClass, constructorDependencies, fieldDependencies, methodDependencies, properties, appliesTo, declaredBy, concerns, sideEffects, constraints );
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
                    properties.add( new PropertyModel( parameterDependency.getKey().getName(), parameterDependency.getKey().getGenericType(), true, false, false ) );
                }
            }
        }

        for( MethodDependency methodDependency : methodDependencies )
        {
            for( ParameterDependency parameterDependency : methodDependency.getParameterDependencies() )
            {
                if( parameterDependency.getKey().getAnnotationType().equals( PropertyParameter.class ) )
                {
                    properties.add( new PropertyModel( parameterDependency.getKey().getName(), parameterDependency.getKey().getGenericType(), true, true, false ) );
                }
            }
        }

        for( FieldDependency fieldDependency : fieldDependencies )
        {
            if( fieldDependency.getKey().getAnnotationType().equals( PropertyField.class ) )
            {
                properties.add( new PropertyModel( fieldDependency.getKey().getName(), fieldDependency.getKey().getGenericType(), true, true, true ) );
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
                K concernModel = (K) modelFactory.newFragmentModel( modifier, compositeType, aClass );
                modifiers.add( concernModel );
            }
        }

        // Check superclass
        if( !aClass.isInterface() && aClass != Object.class )
        {
            List<K> superConcerns = getModifiers( aClass.getSuperclass(), compositeType, annotationClass, modelFactory );
            modifiers.addAll( superConcerns );
        }

        return modifiers;
    }
}