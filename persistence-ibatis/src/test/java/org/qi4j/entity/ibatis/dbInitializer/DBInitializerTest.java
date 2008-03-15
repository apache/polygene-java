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
import static junit.framework.Assert.fail;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.AbstractTestCase;

/**
 * {@code DBInitializerTest} test db initializer.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class DBInitializerTest extends AbstractTestCase
{
    /**
     * Test validity of constructor arguments.
     *
     * @since 0.1.0
     */
    @Test
    public void testConstructor()
    {
        DBInitializerInfo dbInitializerInfo = new DBInitializerInfo( "aURL", new Properties(), null, null );
        try
        {
            new DBInitializer( dbInitializerInfo );
        }
        catch( Exception e )
        {
            fail( "Constructing [DBInitializer] with valid argument must not fail." );
        }

        try
        {
            new DBInitializer( null );
            fail( "Info is [null]. Must throw [IllegalArgumentException]." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( "Info is [null]. Must throw [IllegalArgumentException]." );
        }
    }

    /**
     * Tests the initializer.
     *
     * @throws Exception Thrown if initialization failed.
     * @since 0.1.0
     */
    @Test
    public void testInitializer()
        throws Exception
    {
        initializeDerby();

        DBInitializerInfo info = newDbInitializerInfo();
        DBInitializer initializer = new DBInitializer( info );
        try
        {
            initializer.initialize();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Initialize db must succeed." );
        }

        checkDataInitialization();
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        // Do nothing
    }
}
