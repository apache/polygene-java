package org.qi4j.api.value;

/**
 * Thrown when an error occur during value state (de)serialization.
 */
public class ValueSerializationException
    extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public ValueSerializationException()
    {
        super();
    }

    public ValueSerializationException( String message )
    {
        super( message );
    }

    public ValueSerializationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ValueSerializationException( Throwable cause )
    {
        super( cause.getClass().getName() + ": " + cause.getMessage(), cause );
    }
}
