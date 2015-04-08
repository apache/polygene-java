package org.qi4j.tools.shell.templating;

import java.util.Map;

public class TemplateEngine
{
    private final String template;

    public TemplateEngine( String template )
    {
        this.template = template;
    }

    public String create(Map<String, String> variables)
    {
        StringBuilder builder = new StringBuilder( template.length() * 2 );
        for( int i = 0; i < template.length() - 1; i++ )
        {
            char ch1 = template.charAt( i );
            char ch2 = template.charAt( i + 1 );
            if( ch1 == '@' && ch2 == '@' )
            {
                i = replace( i + 2, builder, variables );
            }
            else
            {
                builder.append( ch1 );
            }
        }
        return builder.toString();
    }

    private int replace( int current, StringBuilder builder, Map<String, String> variables )
    {
        int pos = template.indexOf( '@', current );
        String name = template.substring( current, pos );
        builder.append( variables.get( name ) );
        return pos + 1;
    }
}
