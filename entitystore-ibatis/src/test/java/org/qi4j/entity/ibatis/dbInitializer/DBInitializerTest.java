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
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.AbstractTestCase;

/**
 * {@code DBInitializerTest} test db initializer.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
@Ignore
public final class DBInitializerTest extends AbstractTestCase
{
    public DBInitializerTest( final DBInitializerInfo dbInitializerInfo )
        throws Exception
    {
        this.dbInitializerInfo = dbInitializerInfo;
    }

    private DBInitializerInfo dbInitializerInfo = new DBInitializerInfo( "aURL", new Properties(), null, null );

    @Test public void testValidConstructor()
    {
        new DBInitializer( dbInitializerInfo );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testConstructorNullArgument()
    {
        new DBInitializer( null );
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
        final DBInitializerInfo info = newDbInitializerInfo();
        final DBInitializer initializer = new DBInitializer( info );
        try
        {
            initializer.initialize();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Initialize db must succeed." );
        }

        derbyDatabaseHandler.checkDataInitialization();
    }

    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        // Do nothing
    }
}
