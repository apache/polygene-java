package org.qi4j.library.rest.server.restlet;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.qi4j.api.constraint.ConstraintViolation;

/**
 * TODO
 */
public class ConstraintViolationMessages
{
    public String getMessage( ConstraintViolation violation, Locale locale )
        throws IllegalArgumentException
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle( "ValidationMessages", locale );

            Annotation annotation = violation.constraint();
            String message;
            try
            {
                message = (String) annotation.annotationType().getMethod( "message" ).invoke( annotation );
            }
            catch( Exception e1 )
            {
                message = "{" + annotation.annotationType().getName() + ".message" + "}";
            }

            while( message.startsWith( "{" ) )
            {
                message = bundle.getString( message.substring( 1, message.length() - 1 ) );
            }

            Pattern pattern = Pattern.compile( "\\{[^\\}]*\\}" );
            String result = "";
            Matcher matcher = pattern.matcher( message );
            int start = 0;
            while( matcher.find() )
            {
                String match = matcher.group();
                match = match.substring( 1, match.length() - 1 );
                result += message.substring( start, matcher.start() );

                if( match.equals( "val" ) )
                {
                    result += violation.value() == null ? "null" : violation.value().toString();
                }
                else
                {
                    result += annotation.annotationType().getMethod( match ).invoke( annotation ).toString();
                }
                start = matcher.end();
            }
            result += message.substring( start );
            return result;
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Could not deduce message", e );
        }
    }
}
