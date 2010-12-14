package org.qi4j.spi.property;

/** Super-class for Property related problems.
 *
 */
public abstract class PropertyException extends RuntimeException
{
    public PropertyException( String message )
    {
        super( message );
    }

    public PropertyException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
