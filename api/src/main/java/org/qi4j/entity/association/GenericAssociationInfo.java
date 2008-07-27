/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.entity.association;

import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import org.qi4j.util.MetaInfo;

public class GenericAssociationInfo
    implements AssociationInfo
{
    private Method accessor;
    private MetaInfo metainfo;

    public GenericAssociationInfo( Method accessor, MetaInfo metainfo )
    {
        this.accessor = accessor;
        this.metainfo = metainfo;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metainfo.get( infoType );
    }

    public String name()
    {
        return accessor.getName();
    }

    public String qualifiedName()
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }

    public Type type()
    {
        Type methodReturnType = accessor.getGenericReturnType();
        return getAssociationType( methodReturnType );
    }

    private Type getAssociationType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        Type[] interfaces = ( (Class<?>) methodReturnType ).getInterfaces();
        for( Type anInterface : interfaces )
        {
            Type associationType = getAssociationType( anInterface );
            if( associationType != null )
            {
                return associationType;
            }
        }
        return null;
    }

}
