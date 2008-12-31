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
package org.qi4j.api.entity.association;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.Immutable;

public class GenericAssociationInfo
    implements AssociationInfo
{
    public static String getName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( idx + 1 );
    }

    public static String getDeclaringClassName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( 0, idx + 1 );
    }

    public static String getDeclaringClassName( Method accessor )
    {
        return accessor.getDeclaringClass().getName();
    }

    public static String getQualifiedName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '-' );
        return className + ":" + accessor.getName();
    }

    public static String getQualifiedName( Class<?> declaringClass, String name )
    {
        String className = declaringClass.getName();
        className = className.replace( '$', '-' );
        return className + ":" + name;
    }

    public static Type getAssociationType( Method accessor )
    {
        return getAssociationType( accessor.getGenericReturnType() );
    }

    public static Type getAssociationType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( AbstractAssociation.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        Type[] interfaces = ( (Class<?>) methodReturnType ).getGenericInterfaces();
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

    /**
     * Get URI for an association.
     *
     * @param accessor accessor method
     * @return association URI
     */
    public static String toURI( final Method accessor )
    {
        return toURI( getQualifiedName( accessor ) );
    }

    /**
     * Get URI for an association.
     *
     * @param declaringClass interface of the property
     * @param name of the property
     * @return property URI
     */
    public static String toURI( final Class declaringClass, String name )
    {
        return toURI( getQualifiedName( declaringClass, name ) );
    }

    /**
     * Get URI for a qualified name.
     *
     * @param qualifiedName of the association
     * @return association URI
     */
    public static String toURI( final String qualifiedName )
    {
        return "urn:qi4j:entity:" + qualifiedName.replace( ':', '#' );
    }

    /**
     * Get qualified association name from a URI
     *
     * @param uri of the association
     * @return qualified association name
     */
    public static String toQualifiedName( final String uri )
    {
        return uri.substring( "urn:qi4j:entity:".length() ).replace( '#', ':' );
    }

    /**
     * Get namespace for an association.
     *
     * @param accessor accessor method
     * @return association namespace
     */
    public static String toNamespace( final Method accessor )
    {
        if( accessor == null )
        {
            return null;
        }
        return "urn:qi4j:entity:" + getDeclaringClassName( accessor ) + "#";
    }

    private Method accessor;
    private MetaInfo metainfo;
    private boolean immutable;

    public GenericAssociationInfo( Method accessor, MetaInfo metainfo )
    {
        this.accessor = accessor;
        this.metainfo = metainfo;
        immutable = metainfo.get( Immutable.class ) != null;
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

    public boolean isImmutable()
    {
        return immutable;
    }
}
