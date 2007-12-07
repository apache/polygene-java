package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.composite.scope.PropertyParameter;
import org.qi4j.injection.InjectionScope;
import org.qi4j.injection.Optional;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.ParameterConstraintsModel;
import org.qi4j.spi.composite.ParameterModel;
import org.qi4j.spi.injection.DependencyInjectionModel;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.injection.PropertyInjectionModel;

/**
 * TODO
 */
public abstract class AbstractModelFactory
{
    protected void getConstructorModels( Class mixinClass, List<ConstructorModel> constructorModels )
    {
        Constructor[] constructors = mixinClass.getConstructors();
        for( Constructor constructor : constructors )
        {
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            List<ParameterModel> parameterModels = new ArrayList<ParameterModel>();
            int idx = 0;
            for( Type parameterType : parameterTypes )
            {
                Annotation[] annotations = parameterAnnotations[ idx++ ];
                ParameterModel parameterModel = getParameterModel( annotations, mixinClass, parameterType );
                parameterModels.add( parameterModel );
            }
            ConstructorModel constructorModel = new ConstructorModel( constructor, parameterModels );
            constructorModels.add( constructorModel );
        }
    }

    protected void getFieldModels( Class fragmentClass, List<FieldModel> fieldModels )
    {
        Field[] fields = fragmentClass.getDeclaredFields();
        for( Field field : fields )
        {
            field.setAccessible( true );

            Annotation[] annotations = field.getAnnotations();
            InjectionModel injectionModel = null;
            Annotation annotation = getInjectionAnnotation( annotations );
            if( annotation != null )
            {
                // Only add fields which have injections
                injectionModel = newInjectionModel( annotation, field.getGenericType(), fragmentClass, field );
                FieldModel dependencyModel = new FieldModel( field, injectionModel );
                fieldModels.add( dependencyModel );
            }
        }

        // Add fields in superclass
        Class<?> parent = fragmentClass.getSuperclass();
        if( parent != null && parent != Object.class )
        {
            getFieldModels( parent, fieldModels );
        }
    }


    protected void getConstructorModels( Class mixinClass, Class compositeType, List<ConstructorModel> constructorModels )
    {
        Constructor[] constructors = mixinClass.getConstructors();
        for( Constructor constructor : constructors )
        {
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            List<ParameterModel> parameterModels = new ArrayList<ParameterModel>();
            int idx = 0;
            for( Type parameterType : parameterTypes )
            {
                Annotation[] parameterAnnotation = parameterAnnotations[ idx ];
                ParameterModel parameterModel = getParameterModel( parameterAnnotation, compositeType, parameterType );
                parameterModels.add( parameterModel );
            }
            ConstructorModel constructorModel = new ConstructorModel( constructor, parameterModels );
            constructorModels.add( constructorModel );
        }
    }

    protected Collection<MethodModel> getMethodModels( Class compositeClass )
    {
        List<MethodModel> models = new ArrayList<MethodModel>();
        Method[] methods = compositeClass.getMethods();
        for( Method method : methods )
        {
            MethodModel methodModel = newMethodModel( method, compositeClass );
            models.add( methodModel );
        }

        return models;
    }

    private MethodModel newMethodModel( Method method, Class compositeType )
    {
        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<ParameterModel> parameterModels = new ArrayList<ParameterModel>();
        int idx = 0;
        boolean hasInjections = false;
        for( Type parameterType : parameterTypes )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ idx ];
            ParameterModel parameterModel = getParameterModel( parameterAnnotation, compositeType, parameterType );
            if( parameterModel.getInjectionModel() != null )
            {
                hasInjections = true;
            }
            parameterModels.add( parameterModel );
        }
        MethodModel methodModel = new MethodModel( method, parameterModels, hasInjections );
        return methodModel;
    }

    protected ParameterModel getParameterModel( Annotation[] parameterAnnotation, Class methodClass, Type parameterType )
    {
        InjectionModel injectionModel = null;
        List<Annotation> parameterConstraints = new ArrayList<Annotation>();
        for( Annotation annotation : parameterAnnotation )
        {
            if( annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null )
            {
                parameterConstraints.add( annotation );
            }
            else if( annotation.annotationType().getAnnotation( InjectionScope.class ) != null )
            {
                if( methodClass.isInterface() )
                {
                    throw new InvalidCompositeException( "Not allowed to have injection annotations on interface parameters", methodClass );
                }

                if( injectionModel != null )
                {
                    throw new InvalidCompositeException( "Not allowed to have multiple injection annotations on a single parameter", methodClass );
                }

                injectionModel = newInjectionModel( annotation, parameterType, methodClass, null );
            }
        }
        ParameterConstraintsModel parameterConstraintsModel = null;
        if( parameterConstraints.size() > 0 )
        {
            parameterConstraintsModel = new ParameterConstraintsModel( parameterConstraints );
        }
        ParameterModel parameterModel = new ParameterModel( parameterType, parameterConstraintsModel, injectionModel );
        return parameterModel;
    }

    private InjectionModel newInjectionModel( Annotation annotation, Type injectionType, Class injectedType, Field field )
    {
        InjectionModel model;
        if( annotation.annotationType().equals( PropertyParameter.class ) )
        {
            String name = ( (PropertyParameter) annotation ).value();
            model = new PropertyInjectionModel( annotation.annotationType(), injectionType, injectedType, false, name );
        }
        else if( annotation.annotationType().equals( PropertyField.class ) )
        {
            String name = ( (PropertyField) annotation ).value();
            if( name.equals( "" ) )
            {
                name = field.getName();
            }
            boolean optional = ( (PropertyField) annotation ).optional();
            model = new PropertyInjectionModel( annotation.annotationType(), injectionType, injectedType, optional, name );
        }
        else
        {
            boolean optional = isOptional( annotation );
            model = new DependencyInjectionModel( annotation.annotationType(), injectionType, injectedType, optional );
        }
        return model;
    }


    private boolean isOptional( Annotation annotation )
    {
        Method optionalMethod = getAnnotationMethod( Optional.class, annotation.annotationType() );
        if( optionalMethod != null )
        {
            try
            {
                return Boolean.class.cast( optionalMethod.invoke( annotation ) );
            }
            catch( Exception e )
            {
                throw new InvalidCompositeException( "Could not get optional flag from annotation", annotation.getClass() );
            }
        }
        return false;
    }

    private Method getAnnotationMethod( Class<? extends Annotation> anAnnotationClass, Class<? extends Annotation> aClass )
    {
        Method[] methods = aClass.getMethods();
        for( Method method : methods )
        {
            if( method.getAnnotation( anAnnotationClass ) != null )
            {
                return method;
            }
        }
        return null;
    }

    private Annotation getInjectionAnnotation( Annotation[] parameterAnnotation )
    {
        for( Annotation annotation : parameterAnnotation )
        {
            if( isDependencyAnnotation( annotation ) )
            {
                return annotation;
            }
        }
        return null;
    }

    private boolean isDependencyAnnotation( Annotation annotation )
    {
        return annotation.annotationType().getAnnotation( InjectionScope.class ) != null;
    }

}
