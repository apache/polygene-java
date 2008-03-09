/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis;

import junit.framework.TestCase;
import org.jmock.Mockery;
import static org.qi4j.entity.ibatis.IBatisMutablePropertyInstance.isNotEquals;
import org.qi4j.property.PropertyInfo;

/**
 * {@code IBatisMutablePropertyInstanceTest} tests {@code IBatisMutablePropertyInstance}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public class IBatisMutablePropertyInstanceTest extends TestCase
{
    private Mockery context;

    /**
     * Test constructor.
     *
     * @since 0.1.0
     */
    public final void testConstructor()
    {
        // **************************
        // Test with invalid argument
        // **************************
        String failMsg = "PropertyInfo is [null]. Must throw an IllegalArgumentException.";
        try
        {
            new IBatisMutablePropertyInstance<Object>( null, null );
            fail( failMsg );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( failMsg );
        }

        // ************************
        // Test with valid argument
        // ************************
        PropertyInfo propertyInfo = context.mock( PropertyInfo.class );
        try
        {
            new IBatisMutablePropertyInstance<Object>( propertyInfo, null );
        }
        catch( Exception e )
        {
            fail( "Construct with valid argument. Must not throw any exception." );
        }
    }

    /**
     * Sets set and get, isDirty and markAsClean.
     *
     * @since 0.1.0
     */
    public final void testDirtiness()
    {
        PropertyInfo propertyInfo = context.mock( PropertyInfo.class );
        IBatisMutablePropertyInstance<Object> property =
            new IBatisMutablePropertyInstance<Object>( propertyInfo, null );

        // ******************
        // Test initial value
        // ******************
        assertEquals( "Initial value must be [null].", null, property.get() );

        // ************************************
        // Test set with the same initial value
        // ************************************
        property.set( null );
        assertEquals( "Setting the same value.", null, property.get() );
        assertFalse( "Setting the same value as initial value, must not mark property dirty.", property.isDirty() );

        // *****************************
        // Test set with different value
        // *****************************
        Integer expectedValue1 = 1;
        property.set( expectedValue1 );
        assertEquals( "Must return [1].", expectedValue1, property.get() );
        assertTrue( "Setting different value as initial value, must mark property dirty.", property.isDirty() );

        // --------------------------------------------
        // Test set with the same value as inital value
        // --------------------------------------------
        property.set( null );
        assertEquals( "Must return [null].", null, property.get() );
        assertTrue( "If the property is dirtied before, it must be dirty until mark as clean.", property.isDirty() );

        // *************
        // Sets to clean
        // *************
        property.set( expectedValue1 );
        property.markAsClean();
        assertFalse( "Cleaned property must not be dirty.", property.isDirty() );

        // ------------------------------------
        // Test set with the same initial value
        // ------------------------------------
        property.set( expectedValue1 );
        assertEquals( "Must return [1].", expectedValue1, property.get() );
        assertFalse( "Setting the same value as initial value, must not mark property dirty.", property.isDirty() );

        // ------------------------------------------
        // Test set with the different  initial value
        // ------------------------------------------
        property.set( null );
        assertEquals( "Must return [null].", null, property.get() );
        assertTrue( "Setting different value as initial value, must mark property dirty.", property.isDirty() );
    }

    /**
     * Tests not equals.
     *
     * @since 0.1.0
     */
    public final void testNotEquals()
    {
        assertFalse( isNotEquals( null, null ) );
        assertFalse( isNotEquals( 1, 1 ) );
        assertTrue( isNotEquals( null, 1 ) );
        assertTrue( isNotEquals( 1, null ) );
        assertTrue( isNotEquals( 1, 2 ) );
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        context = new Mockery();
    }

    protected void tearDown() throws Exception
    {
        context = null;
        super.tearDown();
    }
}
