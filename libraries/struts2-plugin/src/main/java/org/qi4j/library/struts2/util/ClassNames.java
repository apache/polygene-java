package org.qi4j.library.struts2.util;

import java.util.Arrays;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;

public final class ClassNames
{
    private ClassNames()
    {
    }

    public static String classNameInDotNotation( Class<?> type )
    {
        Function<Class<?>, String> mapper = ClassNameFilters.passThruMapper;
        Iterable<String> map = Iterables.map( mapper, Arrays.asList( type ) );
        return ClassNames.camelCaseToDotNotation( map );
    }

    public static String classNameInDotNotation( Iterable<Class<?>> type, ClassNameMapper mapper )
    {
        Iterable<String> map = Iterables.map( mapper, type );
        return ClassNames.camelCaseToDotNotation( map );
    }

    public static String camelCaseToDotNotation( Iterable<String> names )
    {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for( String name : names )
        {
            if( count++ > 0 )
            {
                sb.append( "," );
            }
            sb.append( camelCaseToDotNotation( name ) );
        }
        if( count == 1 )
        {
            return sb.toString();
        }
        sb.append( "]" );
        return "[" + sb.toString();
    }

    private static String camelCaseToDotNotation( String name )
    {
        StringBuilder sb = new StringBuilder( name.length() );
        sb.append( toLowerCase( name.charAt( 0 ) ) );
        for( int i = 1; i < name.length(); i++ )
        {
            char c = name.charAt( i );
            if( isLowerCase( c ) )
            {
                sb.append( c );
            }
            else
            {
                sb.append( '.' );
                sb.append( toLowerCase( c ) );
            }
        }
        return sb.toString();
    }
}
