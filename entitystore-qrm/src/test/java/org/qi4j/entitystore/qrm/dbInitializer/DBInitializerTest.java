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
package org.qi4j.entitystore.qrm.dbInitializer;

import java.util.Properties;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.entitystore.qrm.DerbyDatabaseHandler;

/**
 * {@code DBInitializerTest} test db initializer.
 */
public final class DBInitializerTest
{
    private final DerbyDatabaseHandler derbyDatabaseHandler;

    public DBInitializerTest()
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
    }

    /**
     * Tests the initializer.
     *
     * @throws Exception Thrown if initialization failed.
     */
    @Test
    @Ignore( "The entire QRM is buggered." )
    public void testInitializer()
        throws Exception
    {
        final DBInitializerConfiguration info = derbyDatabaseHandler.createDbInitializerConfigMock();
        final DBInitializer initializer = new DBInitializer();
        Properties connectionProperties = info.connectionProperties().get();
        String schemaUrl = info.schemaUrl().get();
        String dataUrl = info.dataUrl().get();
        String dbUrl = info.dbUrl().get();
        initializer.initialize( schemaUrl, dataUrl, dbUrl, connectionProperties );
        derbyDatabaseHandler.checkDataInitialization();
    }

    @After
    public void tearDown()
    {
        if( derbyDatabaseHandler != null )
        {
            derbyDatabaseHandler.shutdown();
        }
    }
}
