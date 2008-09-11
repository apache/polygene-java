package org.qi4j.library.struts2.util;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;


public final class ClassUtil
{
    private ClassUtil() {}

    public static String classNameInDotNotation( Class<?> type )
    {
        return ClassUtil.classNameInDotNotation( type, ClassNameFilters.passThruFilter );
    }

    public static String classNameInDotNotation( Class<?> type, ClassNameFilter filter )
    {
        return ClassUtil.camelCaseToDotNotation( filter.filter( type.getSimpleName() ) );
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
