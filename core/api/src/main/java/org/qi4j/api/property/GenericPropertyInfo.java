/*
 * Copyright 2007,2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.api.property;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.util.Classes;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class GenericPropertyInfo
{
    public static Type getPropertyType( AccessibleObject accessor )
    {
        return getPropertyType( Classes.TYPE_OF.map(accessor) );
    }

    public static Type getPropertyType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( Property.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        if( methodReturnType instanceof Class<?> )
        {
            Type[] interfaces = ( (Class<?>) methodReturnType ).getGenericInterfaces();
            for( Type anInterface : interfaces )
            {
                Type propertyType = getPropertyType( anInterface );
                if( propertyType != null )
                {
                    return propertyType;
                }
            }
        }
        return null;
    }
}
