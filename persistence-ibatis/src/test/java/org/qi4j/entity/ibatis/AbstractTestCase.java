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
import org.apache.derby.drda.NetworkServerControl;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public abstract class AbstractTestCase extends AbstractQi4jTest
{
    private static final String JDBC_URL = "jdbc:derby://localhost/testdb;create=true";
    private static final String DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    private static final String DERBY_USER = "sa";
    private static final String DERBY_PASSWORD = "derbypass";

    private static final String SCHEMA_URL = "testDbSchema.sql";
    private static final String DATA_URL = "testDbData.sql";

    private NetworkServerControl nsc;

    protected AbstractTestCase()
    {
        nsc = null;
    }

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

        Properties dbProperties = new Properties();
        dbProperties.setProperty( "username", DERBY_USER );
        dbProperties.setProperty( "password", DERBY_PASSWORD );
        return new DBInitializerInfo( JDBC_URL, dbProperties, schemaURLString, dataURLString );
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

        Connection connection = getConnection( JDBC_URL, DERBY_USER, DERBY_PASSWORD );
        try
        {
            Statement statement = connection.createStatement();
            try
            {
                statement.execute( "CREATE SCHEMA SA" );
            }
            catch( SQLException e )
            {
                // Ignore
            }

            // Ensure that the all test tables are removed.

            DatabaseMetaData data = connection.getMetaData();
            ResultSet tables = data.getTables( null, null, null, new String[]{ "TABLE" } );
            while( tables.next() )
            {
                String tableName = tables.getString( "TABLE_NAME" );
                try
                {
                    statement.execute( "DROP TABLE " + tableName );
                }
                catch( SQLException e )
                {
                    // Ignore
                }
            }
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
            connection = getJDBCConnection();
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
     * Returns the jdbc connection to test db. Must not return {@code null}.
     *
     * @return The jdbc connection to test db.
     * @throws SQLException Thrown if initializing connection failed.
     * @since 0.1.0
     */
    final Connection getJDBCConnection()
        throws SQLException
    {
        return getConnection( JDBC_URL, DERBY_USER, DERBY_PASSWORD );
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
            Properties systemProperties = System.getProperties();
            systemProperties.setProperty( "derby.drda.securityMechanism", "CLEAR_TEXT_PASSWORD_SECURITY" );

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

    /**
     * Initialize qi4j test application.
     *
     * @param aModule The single module.
     * @since 0.1.0
     */
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
    }
}
