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

import java.io.PrintWriter;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import static java.sql.DriverManager.getConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.derby.drda.NetworkServerControl;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public abstract class AbstractTestCase extends TestCase
{
    protected static final String JDBC_URL = "jdbc:derby://localhost/testdb;create=true";
    protected static final String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";

    protected static final String SCHEMA_URL = "testDbSchema.sql";
    protected static final String DATA_URL = "testDbData.sql";

    private NetworkServerControl nsc;

    /**
     * Construct a new db initializer info.
     *
     * @return a new db initializer info.
     * @since 0.1.0
     */
    protected final DBInitializerInfo newDbInitializerInfo()
    {
        Class aClass = AbstractTestCase.class;
        URL schemaURL = aClass.getResource( SCHEMA_URL );
        assertNotNull( "If run inside ide, make sure sql files are part of project resources.", schemaURL );
        String schemaURLString = schemaURL.toString();

        URL dataURL = aClass.getResource( DATA_URL );
        assertNotNull( "If run inside ide, make sure sql files are part of project resources.", dataURL );
        String dataURLString = dataURL.toString();

        return new DBInitializerInfo( JDBC_URL, new Properties(), schemaURLString, dataURLString );
    }

    /**
     * Initialize derby driver and remove all tables.
     *
     * @throws SQLException Thrown if intialization failed.
     * @since 0.1.0
     */
    protected final void initializeDerby()
        throws SQLException
    {
        // Initialize derby driver.
        try
        {
            Class.forName( DERBY_DRIVER_CLASS_NAME );
        }
        catch( ClassNotFoundException e )
        {
            fail( "Derby client artifact must be included to run this test." );
        }

        // Ensure that the all test tables are removed.
        Connection connection = null;
        try
        {
            connection = getConnection( JDBC_URL, new Properties() );
            DatabaseMetaData data = connection.getMetaData();
            ResultSet tables = data.getTables( null, null, null, new String[]{ "TABLE" } );
            StringBuilder sqlBuidler = new StringBuilder( 0 );
            while( tables.next() )
            {
                String tableName = tables.getString( "TABLE_NAME" );
                sqlBuidler.append( "DROP TABLE " ).append( tableName ).append( "\n" );
            }

            String sqlStatement = sqlBuidler.toString();
            if( sqlStatement.length() > 0 )
            {
                Statement statement = connection.createStatement();
                statement.execute( sqlStatement );
            }
        }
        catch( SQLException e )
        {
            e.printStackTrace();
            fail( "Removing table fails." );
        }
        finally
        {
            if( connection != null )
            {
                connection.close();
            }
        }
    }

    /**
     * Check data initialization.
     *
     * @throws SQLException Thrown if closing connection failed.
     * @since 0.1.0
     */
    protected final void checkDataInitialization()
        throws SQLException
    {
        // Validate that the db is initialized
        Connection connection = null;
        try
        {
            connection = getConnection( JDBC_URL, new Properties() );
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( "SELECT COUNT(*) FROM PERSON" );
            resultSet.next();
            int numberOfRows = resultSet.getInt( 1 );
            assertEquals( "There must be 2 rows in persons table.", 2, numberOfRows );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Fail to validate initialization" );
        }
        finally
        {
            if( connection != null )
            {
                connection.close();
            }
        }
    }

    /**
     * Returns {@code true} if the derby server should be started.
     * By default this method always returns {@code false}.
     *
     * @return A {@code boolean} indicator whether the derby server should be started.
     * @since 0.1.0
     */
    protected boolean isDerbyServerShouldBeStarted()
    {
        return false;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        if( isDerbyServerShouldBeStarted() )
        {
            nsc = new NetworkServerControl();
            PrintWriter logOuput = new PrintWriter( out );
            nsc.start( logOuput );

            // Wait until server started up
            waitUntilDerbyStarted();
        }
    }


    /**
     * Wait until derby started.
     *
     * @throws InterruptedException Thrown if sleep fails.
     * @since 0.1.0
     */
    private void waitUntilDerbyStarted()
        throws InterruptedException
    {
        int count = 0;
        while( true )
        {
            // If we retries 3 times
            if( count == 2 )
            {
                fail( "DB is not started after waiting for [150] ms" );
                break;
            }

            try
            {
                nsc.ping();
                break;
            }
            catch( Exception e )
            {
                // Sleep for 50 ms before restart
                sleep( 50 );


            }
            count++;
        }
    }

    protected void tearDown()
        throws Exception
    {
        if( nsc != null )
        {
            nsc.shutdown();
            nsc = null;
        }

        super.tearDown();
    }
}
