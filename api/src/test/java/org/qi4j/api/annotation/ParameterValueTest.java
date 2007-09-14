package org.qi4j.api.annotation;
/**
 *  TODO
 */

import junit.framework.TestCase;
import static org.qi4j.api.annotation.ParameterValue.name;
import static org.qi4j.api.annotation.ParameterValue.parameter;
import org.qi4j.api.model.Mixin1;

public class ParameterValueTest extends TestCase
{
    ParameterValue parameterValue;

    public void testParameter()
        throws Exception
    {
        // Just create a parameter
        ParameterValue value1 = parameter( "bar", "foo");

        // Create a parameter using the refactoring-safe method
        ParameterValue value2 = parameter( name(Mixin1.class).getBar(), "foo");


        assertEquals( value1, value2);
    }
}