package org.qi4j.composite;
/**
 *  TODO
 */

import junit.framework.TestCase;
import static org.qi4j.composite.PropertyValue.name;
import static org.qi4j.composite.PropertyValue.property;

public class PropertyValueTest extends TestCase
{
    PropertyValue propertyValue;

    public void testProperty() throws Exception
    {
        // Just create a property
        PropertyValue value1 = property( "bar", "foo" );

        // Create a property using the refactoring-safe method
        PropertyValue value2 = property( name( Mixin1.class ).getBar(), "foo" );


        assertEquals( value1, value2 );
    }
}