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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;

public final class GenericPropertyInfo
    implements PropertyInfo, Serializable
{
    public static Type getPropertyType( Method accessor )
    {
        return getPropertyType( accessor.getGenericReturnType() );
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

    private MetaInfo infos;
    private boolean immutable;
    private boolean computed;
    private final QualifiedName qualifiedName;
    private final Type type;

    public GenericPropertyInfo( Method accessor )
    {
        this.qualifiedName = QualifiedName.fromMethod( accessor );
        this.type = getPropertyType( accessor );
        infos = new MetaInfo().withAnnotations( accessor );
        immutable = metaInfo( Immutable.class ) != null;
        computed = metaInfo( Computed.class ) != null;
    }

    public GenericPropertyInfo( Class declaringClass, String accessorName )
    {
        try
        {
            Method accessor = declaringClass.getMethod( accessorName );
            this.qualifiedName = QualifiedName.fromMethod( accessor );
            this.type = getPropertyType( accessor );
            infos = new MetaInfo().withAnnotations( accessor );
            immutable = metaInfo( Immutable.class ) != null;
            computed = metaInfo( Computed.class ) != null;
        }
        catch( NoSuchMethodException e )
        {
            //noinspection ThrowableInstanceNeverThrown
            throw (InternalError) new InternalError().initCause( e );
        }
    }

    public GenericPropertyInfo( MetaInfo infos,
                                boolean immutable,
                                boolean computed,
                                QualifiedName qualifiedName,
                                Type type
    )
    {
        this.infos = infos;
        this.immutable = immutable;
        this.computed = computed;
        this.qualifiedName = qualifiedName;
        this.type = type;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return infos.get( infoType );
    }

    public String name()
    {
        return qualifiedName.name();
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isComputed()
    {
        return computed;
    }
}
