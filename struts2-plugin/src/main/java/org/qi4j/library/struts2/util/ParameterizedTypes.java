package org.qi4j.library.struts2.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ParameterizedTypes
{
    private ParameterizedTypes()
    {
    }

    public static <S, T extends S> Type[] findTypeVariables( Class<T> type, Class<S> searchType )
    {
        return ParameterizedTypes.findParameterizedType( type, searchType ).getActualTypeArguments();
    }

    public static <S, T extends S> ParameterizedType findParameterizedType( Class<T> type, Class<S> searchType )
    {
        return ParameterizedTypes.findParameterizedType( (Type) type, searchType );
    }

    static ParameterizedType findParameterizedType( Type type, Type searchType )
    {
        if( type instanceof ParameterizedType && ( (ParameterizedType) type ).getRawType().equals( searchType ) )
        {
            return (ParameterizedType) type;
        }
        Type[] parents = ( (Class<?>) type ).getGenericInterfaces();
        for( Type parent : parents )
        {
            ParameterizedType foundType = findParameterizedType( parent, searchType );
            if( foundType != null )
            {
                return foundType;
            }
        }
        return null;
    }
}
