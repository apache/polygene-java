package org.qi4j.library.general.model;

/**
 * Default exception for validation error
 */
public class ValidationException extends RuntimeException
{
    public ValidationException( String message )
    {
        super( message );
    }

    public ValidationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
