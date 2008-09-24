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
import java.lang.reflect.Method;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.composite.Composite;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public class Util
{
    public static String objectToString( Object anObject )
    {
        if( anObject == null )
        {
            return "";
        }

        Class<?> valueClass = anObject.getClass();
        if( String.class.isAssignableFrom( valueClass ) )
        {
            return (String) anObject;
        }

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
            return compositeClass.getName();
        }
        else if( ObjectDetailDescriptor.class.isAssignableFrom( valueClass ) )
        {
            ObjectDetailDescriptor detailDescriptor = (ObjectDetailDescriptor) anObject;
            ObjectDescriptor descriptor = detailDescriptor.descriptor();
            Class<? extends Composite> objectClassName = descriptor.type();
            return objectClassName.getName();
        }

        return anObject.toString();
    }

    public static String methodToString( Method method )
    {
        StringBuilder buf = new StringBuilder();

        for( Annotation annotation : method.getAnnotations() )
        {
            appendAnnotation( buf, annotation );
        }
        Class<?> returnType = method.getReturnType();

        // todo add the 'Type' if the returnType is Property<T>

        buf.append( returnType.getSimpleName() ).append( " " ).append( method.getName() );

        buf.append( "( " );
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for( int i = 0; i < paramTypes.length; i++ )
        {
            Annotation[] annotations = paramAnnotations[ i ];
            for( Annotation annotation : annotations )
            {
                appendAnnotation( buf, annotation );
            }
            Class<?> type = paramTypes[ i ];
            buf.append( type.getSimpleName() ).append( ", " );
        }
        if( paramTypes.length > 0 )
        {
            buf.delete( buf.length() - 2, buf.length() );
        }

        buf.append( " )" );

        return buf.toString();
    }

    private static void appendAnnotation( StringBuilder buf, Annotation annotation )
    {
        buf.append( "@" ).append( annotation.annotationType().getSimpleName() ).append( " " );
    }
}
