package org.qi4j.spi.property;

/**
 * Thrown when one tries to set a property via the model architecture, without that property being present in the
 * composite.
 */
public class PropertyDoesNotExistException
    extends PropertyException
{
    public PropertyDoesNotExistException( String message )
    {
        super( message );
    }
}
