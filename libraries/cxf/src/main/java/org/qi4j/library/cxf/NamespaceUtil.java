/*
 * Copyright 2010 Niclas Hedhman.
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

package org.qi4j.library.cxf;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.xml.namespace.QName;

/**
 *
 */
public final class NamespaceUtil
{

    private NamespaceUtil()
    {
    }

    public static QName convertJavaTypeToQName( Type type )
    {
        if( type instanceof Class )
        {
            Class<?> valueType = (Class<?>) type;
            String packageName = valueType.getPackage().getName();
            String className = valueType.getSimpleName();
            QName typeQName = new QName( "urn:qi4j:type:value:" + packageName, className );
            return typeQName;
        }
        else if( type instanceof ParameterizedType )
        {
            return convertJavaTypeToQName( ( (ParameterizedType) type ).getRawType() );
        }
        else
        {
            throw new UnsupportedOperationException(
                "Types that are not Class nor ParameterizedType are not yet supported:" + type );
        }
    }
}
