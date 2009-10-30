package org.qi4j.library.struts2.util;

import static java.lang.Character.*;

public final class ClassNames
{
    private ClassNames()
    {
    }

    public static String classNameInDotNotation( Class<?> type )
    {
        return ClassNames.classNameInDotNotation( type, ClassNameFilters.passThruFilter );
    }

    public static String classNameInDotNotation( Class<?> type, ClassNameFilter filter )
    {
        return ClassNames.camelCaseToDotNotation( filter.filter( type.getSimpleName() ) );
    }

    public static String camelCaseToDotNotation( String name )
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
