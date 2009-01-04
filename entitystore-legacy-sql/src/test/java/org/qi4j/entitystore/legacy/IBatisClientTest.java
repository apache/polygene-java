package org.qi4j.entitystore.legacy;

import com.ibatis.common.jdbc.ScriptRunner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.entitystore.legacy.entity.PersonComposite;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.entitystore.legacy.LegacySqlClient;

/**
 * @author mh14 @ jexp.de
 * @since 10.06.2008 11:30:49 (c) 2008 jexp.de
 */
public class IBatisClientTest
{
    private DerbyDatabaseHandler databaseHandler;
    private LegacySqlClient client;
    private static final String TEST_ID = TestConfig.JANE_SMITH_ID;

    @Test public void loadExisingPerson() throws Exception
    {
        final QualifiedIdentity id = new QualifiedIdentity( TEST_ID, PersonComposite.class );
        assertEquals( "id", TEST_ID, id.identity() );
        final Map<String, Object> rawData = client.executeLoad( id );
        System.out.println( "rawData = " + rawData );
        assertEquals( "id gefunden", TEST_ID, rawData.get( "id" ) );
        assertEquals( "last name", "Smith", rawData.get( "lastName" ) );
        final Object accountsValue = rawData.get( "accounts" );
        assertTrue( "accounts", accountsValue instanceof Collection );
        final Collection accounts = (Collection) accountsValue;
        assertEquals( "one account ", 2, accounts.size() );
        assertEquals( "accounts", asList( "1", "2" ), accounts );
    }

    @Before
    public void setUp() throws Exception
    {
        databaseHandler = new DerbyDatabaseHandler();
        setupDatabase( databaseHandler.getJDBCConnection() );
        client = new LegacySqlClient( databaseHandler.getUrlString( TestConfig.SQL_MAP_CONFIG_XML ), null );
        client.activate();
    }

    private void setupDatabase( Connection jdbcConnection )
        throws SQLException, IOException
    {
        final ScriptRunner runner = new ScriptRunner( jdbcConnection, true, true );
        runScript( runner, TestConfig.SCHEMA_FILE );
        runScript( runner, TestConfig.DATA_FILE );
    }

    private void runScript( ScriptRunner runner, String file )
        throws IOException, SQLException
    {
        runner.runScript( new InputStreamReader( new URL( databaseHandler.getUrlString( file ) ).openStream() ) );
    }

    @After public void shutdown()
    {
        if( databaseHandler != null )
        {
            databaseHandler.shutdown();
        }
    }
}
