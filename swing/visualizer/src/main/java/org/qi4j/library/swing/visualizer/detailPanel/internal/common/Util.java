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

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * @author edward.yakop@gmail.com
 */
public class Util
{

    public static String methodToString( Method method )
    {
        StringBuilder buf = new StringBuilder();

        for( Annotation annotation : method.getAnnotations() )
        {
            appendAnnoation( buf, annotation );
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
                appendAnnoation( buf, annotation );
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

    private static void appendAnnoation( StringBuilder buf, Annotation annotation )
    {
        buf.append( "@" ).append( annotation.annotationType().getSimpleName() ).append( " " );
    }
}
