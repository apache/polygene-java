/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer.detailPanel.internal.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import org.qi4j.composite.Composite;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.InjectedFieldDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.InjectedMethodDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.composite.InjectedFieldDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ToStringUtils
{
    private ToStringUtils()
    {
    }

    public static String typeToString( Type aType )
    {
        if( aType == null )
        {
            return null;
        }

        Class<? extends Type> typeClass = aType.getClass();
        if( GenericArrayType.class.isAssignableFrom( typeClass ) )
        {
            GenericArrayType gType = (GenericArrayType) aType;

            Type type = gType.getGenericComponentType();
            return "GenericArrayType [" + typeToString( type ) + "]";
        }
        else if( TypeVariable.class.isAssignableFrom( typeClass ) )
        {
            TypeVariable tType = (TypeVariable) aType;

            // TODO: Add all type properties?
            return "TypeVariable [" + tType.getName() + "]";
        }
        else if( WildcardType.class.isAssignableFrom( typeClass ) )
        {
            WildcardType wType = (WildcardType) aType;

            // TODO: We probably need to expand one by one
            return "WildcardType LowerBounds [" +
                   Arrays.toString( wType.getLowerBounds() ) +
                   "] UpperBounds [" +
                   Arrays.toString( wType.getUpperBounds() ) +
                   "]";
        }
        else if( ParameterizedType.class.isAssignableFrom( typeClass ) )
        {
            ParameterizedType pType = (ParameterizedType) aType;
            return "ParamaterizedType [" + typeToString( pType.getRawType() ) + "]";
        }
        else if( Class.class.isAssignableFrom( typeClass ) )
        {
            Class clazz = (Class) aType;
            return clazz.getName();
        }

        return aType.toString();
    }

    public static String objectToString( Object anObject )
    {
        if( anObject == null )
        {
            return "";
        }

        Class<?> valueClass = anObject.getClass();
        if( ApplicationDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ApplicationDetailDescriptor detailDescriptor = (ApplicationDetailDescriptor) anObject;
            ApplicationDescriptor descriptor = detailDescriptor.descriptor();
            return descriptor.name();
        }
        else if( ServiceDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ServiceDetailDescriptor detailDescriptor = (ServiceDetailDescriptor) anObject;
            ServiceDescriptor descriptor = detailDescriptor.descriptor();
            Class<?> serviceClass = descriptor.type();
            String serviceClassName = serviceClass.getSimpleName();
            return serviceClassName + ":" + descriptor.identity();
        }
        else if( EntityDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            EntityDetailDescriptor detailDescriptor = (EntityDetailDescriptor) anObject;
            EntityDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> entityClass = descriptor.type();
            return entityClass.getName();
        }
        else if( CompositeDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            CompositeDetailDescriptor detailDescriptor = (CompositeDetailDescriptor) anObject;
            CompositeDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> compositeClass = descriptor.type();
            return compositeClass.getSimpleName();
        }
        else if( ObjectDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ObjectDetailDescriptor detailDescriptor = (ObjectDetailDescriptor) anObject;
            ObjectDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> objectClassName = descriptor.type();
            return objectClassName.getName();
        }
        else if( InjectedFieldDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            InjectedFieldDetailDescriptor detailDescriptor = (InjectedFieldDetailDescriptor) anObject;
            InjectedFieldDescriptor descriptor = detailDescriptor.descriptor();
            Field field = descriptor.field();
            return fieldToString( descriptor, field );
        }
        else if( InjectedMethodDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            InjectedMethodDetailDescriptor detailDescriptor = (InjectedMethodDetailDescriptor) anObject;
            Method injectedMethod = detailDescriptor.descriptor().method();
            return methodToString( injectedMethod );
        }
        else if( CompositeMethodDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            CompositeMethodDetailDescriptor detailDescriptor = (CompositeMethodDetailDescriptor) anObject;
            Method method = detailDescriptor.descriptor().method();
            return methodToString( method );
        }

        return anObject.toString();
    }

    private static String fieldToString( InjectedFieldDescriptor descriptor, Field field )
    {
        StringBuilder builder = new StringBuilder();
        Annotation[] annotations = field.getAnnotations();
        appendAnnotation( builder, annotations );
        builder.append( field.getType().getSimpleName() );
        builder.append( field.getName() );
        return builder.toString();
    }

    public static String methodToString( Method method )
    {
        StringBuilder builder = new StringBuilder();

        // Method annotations
        Annotation[] methodAnnotations = method.getAnnotations();
        appendAnnotation( builder, methodAnnotations );
        Class<?> returnType = method.getReturnType();

        // Method return type
        // TODO: add the 'Type' if the returnType is Property<T>
        builder.append( returnType.getSimpleName() ).append( " " );

        // Method name
        builder.append( method.getName() );

        // Method parameters
        appendMethodParameters( method, builder );

        return builder.toString();
    }

    private static void appendAnnotation( StringBuilder buf, Annotation... annotations )
    {
        for( Annotation annotation : annotations )
        {
            String annotationName = annotation.annotationType().getSimpleName();
            buf.append( "@" ).append( annotationName ).append( " " );
        }
    }

    private static void appendMethodParameters( Method method, StringBuilder builder )
    {
        builder.append( "(" );
        Class<?>[] paramTypes = method.getParameterTypes();

        int numberOfParams = paramTypes.length;
        if( numberOfParams > 0 )
        {
            builder.append( ' ' );
        }

        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for( int i = 0; i < numberOfParams; i++ )
        {
            Annotation[] annotations = paramAnnotations[ i ];
            appendAnnotation( builder, annotations );

            Class<?> type = paramTypes[ i ];
            builder.append( type.getSimpleName() );

            if( i < numberOfParams - 1 )
            {
                builder.append( ", " );
            }
        }
        builder.append( " )" );
    }
}
