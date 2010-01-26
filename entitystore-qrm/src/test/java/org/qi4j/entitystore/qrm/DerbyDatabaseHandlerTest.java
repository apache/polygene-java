/*
 * Copyright (c) 2008, Michael Hunger. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.entitystore.qrm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class DerbyDatabaseHandlerTest
{
    private DerbyDatabaseHandler derbyDatabaseHandler;

    @Test
    @Ignore( "The entire QRM is buggered." )
    public void testInitDerby()
        throws SQLException
    {
        final Connection connection = derbyDatabaseHandler.getJDBCConnection();
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        assertNotNull( "DataBaseMetaData from initialized DB", databaseMetaData );
    }

    @After
    public void tearDown()
        throws Exception
    {
        if( derbyDatabaseHandler != null )
        {
            derbyDatabaseHandler.shutdown();
        }
    }

    @Before
    public void setUp()
        throws Exception
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
    }
}
