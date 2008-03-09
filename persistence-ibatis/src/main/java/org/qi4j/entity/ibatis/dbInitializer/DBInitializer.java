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

import com.ibatis.common.jdbc.ScriptRunner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import static org.qi4j.composite.NullArgumentException.validateNotNull;

/**
 * {@code DBIntializer} initialize the db.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class DBInitializer
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final DBInitializerInfo dBInitializerInfo;

    /**
     * Construct an instance of {@code DBInitializerInfo}.
     *
     * @param aDBInitializerInfo The db initializer info. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aDBInitializerInfo} argument is {@code null}.
     * @since 0.1.0
     */
    public DBInitializer( DBInitializerInfo aDBInitializerInfo )
        throws IllegalArgumentException
    {
        dBInitializerInfo = aDBInitializerInfo;
        validateNotNull( "aDBInitializerInfo", aDBInitializerInfo );
    }

    /**
     * Initialize the database.
     *
     * @throws java.sql.SQLException Thrown if db initialization failed.
     * @throws java.io.IOException   Thrown if reading schema or data sql resources failed.
     * @since 0.1.0
     */
    public final void initialize()
        throws SQLException, IOException
    {
        String schemaURLString = dBInitializerInfo.getSchemaURL();
        String dbDataURLString = dBInitializerInfo.getDataURL();

        // Make sure at least one of the url exists.
        if( schemaURLString == null && dbDataURLString == null )
        {
            return;
        }

        Connection connection = getSqlConnection();
        ScriptRunner runner = new ScriptRunner( connection, true, true );

        // Initialize schema if exists
        if( schemaURLString != null )
        {
            URL schemaURL = new URL( schemaURLString );
            InputStreamReader inputStreamReader = new InputStreamReader( schemaURL.openStream() );
            BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            runner.runScript( bufferedReader );
        }

        // Initialize data if exists
        if( dbDataURLString != null )
        {
            URL dbDURl = new URL( dbDataURLString );
            InputStreamReader inputStreamReader = new InputStreamReader( dbDURl.openStream() );
            BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
            runner.runScript( bufferedReader );
        }
    }

    /**
     * Get the sql connection.
     *
     * @return The sql connection.
     * @throws SQLException Thrown if sql connection failed.
     * @since 0.1.0
     */
    private Connection getSqlConnection()
        throws SQLException
    {
        String dbURL = dBInitializerInfo.getDbURL();
        Properties dbProperties = dBInitializerInfo.getConnectionProperties();
        return DriverManager.getConnection( dbURL, dbProperties );
    }
}
