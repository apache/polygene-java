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

    static interface Mixin1
    {
        void setName( String name );

        void setBar( String bar );

        String getName();

        String getBar();
    }

    interface Mixin2
    {
    }

    public interface Mixin3
    {
    }


    public class Mixin1Impl implements Mixin1
    {
        private String name;
        private String description;
        private String bar;

        public Mixin1Impl( String name, String bar, String str )
        {
            this.bar = bar;
            this.name = name;
            this.description = str;
        }

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public String getBar()
        {
            return bar;
        }

        public void setBar( String bar )
        {
            this.bar = bar;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription( String description )
        {
            this.description = description;
        }
    }

    static class Mixin2Impl
        implements Mixin2
    {
        public Mixin2Impl( String str )
        {
            //TODO: Auto-generated, need attention.

        }
    }

    static class Mixin3Impl
        implements Mixin3
    {
    }
}