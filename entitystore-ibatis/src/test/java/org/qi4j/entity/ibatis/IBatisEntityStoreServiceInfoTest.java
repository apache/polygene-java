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

import java.util.Properties;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import org.junit.Test;

/**
 * {@code IBatisEntityStoreServiceInfoTest} tests {@code IBatisEntityStoreServiceInfo}.
 *
 * @author edward.yakop@gmail.com
 */
public final class IBatisEntityStoreServiceInfoTest
{
    private static final String SOME_URL = "aURL";

    /**
     * Test constructors.
     *
     * @since 0.1.0
     */
    @Test
    public final void testConstructors()
    {
        // ***************************
        // Test with invalid arguments
        // ***************************
        String failMsg = "URL argument is [null]. Must throw [IllegalArgumentException].";
        try
        {
            new IBatisEntityStoreServiceInfo( null );
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

        try
        {
            new IBatisEntityStoreServiceInfo( null, null );
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

        Properties blankProperties = new Properties();
        try
        {
            new IBatisEntityStoreServiceInfo( null, blankProperties );
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

        // *************************
        // Test with valid arguments
        // *************************
        try
        {
            new IBatisEntityStoreServiceInfo( SOME_URL );
        }
        catch( Exception e )
        {
            fail( "Construct with valid arguments. Must not throw any exception." );
        }

        try
        {
            new IBatisEntityStoreServiceInfo( SOME_URL, null );
        }
        catch( Exception e )
        {
            fail( "Construct with valid arguments. Must not throw any exception." );
        }

        try
        {
            new IBatisEntityStoreServiceInfo( SOME_URL, blankProperties );
        }
        catch( Exception e )
        {
            fail( "Construct with valid arguments. Must not throw any exception." );
        }

    }

    /**
     * Test {@link IBatisEntityStoreServiceInfo#getConfigProperties()} and
     * {@link IBatisEntityStoreServiceInfo#getConfigProperties()}.
     *
     * @since 0.1.0
     */
    public void testGetters()
    {
        // ********************************
        // Test with null config properties
        // ********************************
        IBatisEntityStoreServiceInfo info1 = new IBatisEntityStoreServiceInfo( SOME_URL );
        assertEquals( SOME_URL, info1.getSQLMapConfigURL() );
        assertEquals( null, info1.getConfigProperties() );

        IBatisEntityStoreServiceInfo info2 = new IBatisEntityStoreServiceInfo( SOME_URL, null );
        assertEquals( SOME_URL, info2.getSQLMapConfigURL() );
        assertEquals( null, info2.getConfigProperties() );

        // ************************************
        // Test with non-null config properties
        // ************************************
        Properties blankProperties = new Properties();
        IBatisEntityStoreServiceInfo info3 = new IBatisEntityStoreServiceInfo( SOME_URL, blankProperties );
        assertEquals( SOME_URL, info3.getSQLMapConfigURL() );
        assertEquals( blankProperties, info3.getConfigProperties() );
    }
}
