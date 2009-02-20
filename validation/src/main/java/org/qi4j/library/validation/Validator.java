package org.qi4j.library.validation;

import java.util.List;

/**
 * JAVADOC
 */
public class Validator
{
    List<ValidationMessage> messages;
    String resourceBundle;

    public Validator( List<ValidationMessage> messages, String resourceBundle )
    {
        this.messages = messages;
        this.resourceBundle = resourceBundle;
    }

    public void error( boolean test, String resourceKey, Object... arguments )
    {
        if( test )
        {
            messages.add( new ValidationMessage( resourceKey, resourceBundle, ValidationMessage.Severity.ERROR, arguments ) );
        }
    }

    public void warn( boolean test, String resourceKey, Object... arguments )
    {
        if( test )
        {
            messages.add( new ValidationMessage( resourceKey, resourceBundle, ValidationMessage.Severity.WARNING, arguments ) );
        }
    }

    public void info( boolean test, String resourceKey, Object... arguments )
    {
        if( test )
        {
            messages.add( new ValidationMessage( resourceKey, resourceBundle, ValidationMessage.Severity.INFO, arguments ) );
        }
    }
}
