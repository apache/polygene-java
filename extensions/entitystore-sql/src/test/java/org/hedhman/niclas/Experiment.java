package org.hedhman.niclas;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TransactionProvider;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.impl.ThreadLocalTransactionProvider;
import org.junit.Test;

public class Experiment
{
    @Test
    public void test1()
        throws Exception
    {
        String host = "127.0.0.1";
        int port = 3306;
        DataSource dataSource = dbcpDataSource( host, port );
        Settings settings = new Settings().withRenderNameStyle( RenderNameStyle.QUOTED );
        SQLDialect dialect = SQLDialect.MARIADB;

        ConnectionProvider connectionProvider = new DataSourceConnectionProvider( dataSource );
        TransactionProvider transactionProvider = new ThreadLocalTransactionProvider( connectionProvider, false );
        Configuration configuration = new DefaultConfiguration()
            .set( dialect )
            .set( connectionProvider )
            .set( transactionProvider )
            .set( settings );

        DSLContext dsl = DSL.using( configuration );

        Field<String> identityColumn = DSL.field( DSL.name( "_identity" ), SQLDataType.VARCHAR );
        Name entitiesTableName = DSL.name( "ENTITIES" );
        Table<Record> entitiesTable = new TableImpl<Record>( entitiesTableName );
        dsl.transaction( t -> {

            dsl.createTableIfNotExists( entitiesTable )
               .column( identityColumn )
               .execute();
        });

        dsl.transaction( t -> {
            dsl.insertInto( entitiesTable )
               .set( identityColumn, "12" )
               .execute();
        });
    }

    private DataSource dbcpDataSource( String host, int port )
        throws Exception
    {
        BasicDataSource pool = new BasicDataSource();

        String driverClass = "com.mysql.jdbc.Driver";
        Class.forName( driverClass );
        pool.setDriverClassName( driverClass );
        pool.setUrl( "jdbc:mysql://" + host + ":" + port + "/testdb" );
        pool.setUsername( "root" );
        pool.setPassword( "testing" );
        pool.setDefaultAutoCommit( false );
        return pool;
    }
}
