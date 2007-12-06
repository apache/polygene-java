/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.annotation.scope.Optional;
import org.qi4j.annotation.scope.PropertyField;
import org.qi4j.annotation.scope.PropertyParameter;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.injection.DependencyInjectionModel;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.injection.PropertyInjectionModel;

/**
 * TODO
 */
public class InjectionModelFactory
{
    public InjectionModel newInjectionModel( Annotation annotation, Type injectionType, Class injectedType )
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

}
