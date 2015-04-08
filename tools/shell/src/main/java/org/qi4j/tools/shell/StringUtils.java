package org.qi4j.tools.shell;

public class StringUtils
{
    private StringUtils()
    {
    }

    public static String camelCase( String text, boolean firstUpper )
    {
        StringBuilder builder = new StringBuilder( text.length() );
        boolean initial = firstUpper;
        for( int i = 0; i < text.length(); i++ )
        {
            char ch = text.charAt( i );
            if( initial )
            {
                ch = Character.toUpperCase( ch );
                initial = false;
            }
            if( ch != ' ' )
            {
                builder.append( ch );
            }
            else
            {
                initial = true;
            }
        }
        return builder.toString();
    }
}
