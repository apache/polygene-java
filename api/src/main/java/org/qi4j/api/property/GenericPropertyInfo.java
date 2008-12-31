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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.api.common.MetaInfo;

public final class GenericPropertyInfo
    implements PropertyInfo
{
    public static String getName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( idx + 1 );
    }

    public static String getDeclaringClassName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( 0, idx );
    }

    public static String getDeclaringClassName( Method accessor )
    {
        return accessor.getDeclaringClass().getName();
    }

    public static String getQualifiedName( Method accessor )
    {
        return getQualifiedName( accessor.getDeclaringClass(), accessor.getName() );
    }

    public static String getQualifiedName( Class<?> declaringClass, String name )
    {
        String className = declaringClass.getName();
        className = className.replace( '$', '-' );
        return className + ":" + name;
    }

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

    /**
     * Get URI for a property.
     *
     * @param accessor accessor method
     * @return property URI
     */
    public static String toURI( final Method accessor )
    {
        return toURI( getQualifiedName( accessor ) );
    }

    /**
     * Get URI for a property.
     *
     * @param declaringClass of the property
     * @param name of the property
     * @return property URI
     */
    public static String toURI( final Class declaringClass, String name )
    {
        return toURI( getQualifiedName( declaringClass, name ) );
    }

    /**
     * Get URI for a qualified property name.
     *
     * @param qualifiedName of the property
     * @return property URI
     */
    public static String toURI( final String qualifiedName )
    {
        return "urn:qi4j:entity:" + qualifiedName.replace( ':', '#' );
    }

    /**
     * Get qualified property name from a URI
     *
     * @param uri of the property
     * @return property qualified property name
     */
    public static String toQualifiedName( final String uri )
    {
        return uri.substring( "urn:qi4j:entity:".length() ).replace( '#', ':' );
    }

    /**
     * Get namespace for a property.
     *
     * @param accessor accessor method
     * @return property namespace
     */
    public static String toNamespace( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:entity:" + getDeclaringClassName( accessor ) + "#";
    }

    private MetaInfo infos;
    private boolean immutable;
    private boolean computed;
    private final String qualifiedName;
    private final String name;
    private final Type type;

    public GenericPropertyInfo( Method accessor )
    {
        this.qualifiedName = getQualifiedName( accessor );
        this.name = getName( qualifiedName );
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

            this.qualifiedName = getQualifiedName( accessor );
            this.name = getName( qualifiedName );
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

    public GenericPropertyInfo( MetaInfo infos, boolean immutable, boolean computed, String qualifiedName, String name, Type type )
    {
        this.infos = infos;
        this.immutable = immutable;
        this.computed = computed;
        this.qualifiedName = qualifiedName;
        this.name = name;
        this.type = type;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return infos.get( infoType );
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
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
