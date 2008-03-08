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
package org.qi4j.entity.ibatis.dbInitializer;

import java.util.Properties;
import junit.framework.TestCase;

/**
 * {@code DBInitializerInfoTest} tests {@code DBInitializerInfo}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class DBInitializerInfoTest extends TestCase
{
    /**
     * Test validitiy of constructors arguments.
     *
     * @since 0.1.0
     */
    public final void testConstructor()
    {
        try
        {
            new DBInitializerInfo( "aURL", new Properties(), null, null );
        }
        catch( Exception e )
        {
            fail( "Creating info with valid arguments must not fail." );
        }

        try
        {
            new DBInitializerInfo( null, new Properties(), "schemaURL", "dataURL" );
            fail( "DBUrl is [null]. Must throw IllegalArgumentException." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( "DBUrl is [null]. Must throw IllegalArgumentException" );
        }

        try
        {
            new DBInitializerInfo( "aURL", null, "schemaURL", "dataURL" );
            fail( "dbProperties is [null]. Must throw IllegalArgumentException." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( "dbProperties is [null]. Must throw IllegalArgumentException." );
        }
    }

    /**
     * Test getters.
     */
    public final void testGetters()
    {
        String dbURL = "aURL";
        Properties properties = new Properties();
        String schemaURL = "schemaURL";
        String dataURL = "dataURL";
        DBInitializerInfo info = new DBInitializerInfo( dbURL, properties, schemaURL, dataURL );

        assertEquals( dbURL, info.getDbURL() );
        assertEquals( properties, info.getConnectionProperties() );
        assertEquals( schemaURL, info.getSchemaURL() );
        assertEquals( dataURL, info.getDataURL() );
    }
}
