package org.qi4j.api.property;

import java.lang.reflect.AccessibleObject;
import org.qi4j.api.common.ConstructionException;

/**
 * Thrown when attempting to subclass Property.
 */
public class InvalidPropertyTypeException extends ConstructionException
{
    public InvalidPropertyTypeException( AccessibleObject accessor )
    {
        super( createMessage(accessor) );
    }

    private static String createMessage( AccessibleObject accessor )
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Not allowed to subclass " )
            .append( Property.class.getName() )
            .append( ". Property accessor " )
            .append( accessor )
            .append( " is returning a Property subclass." );
        return builder.toString();
    }
}
