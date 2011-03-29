package org.qi4j.library.sql.liquibase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.Test;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.library.sql.datasource.DataSourceConfiguration;
import org.qi4j.library.sql.datasource.DataSourceService;
import org.qi4j.library.sql.datasource.Databases;
import org.qi4j.spi.service.importer.ServiceInstanceImporter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qi4j.api.io.Outputs.*;
import static org.qi4j.api.io.Transforms.*;

/**
 * Test DataSource and Liquibase services
 */
public class LiquibaseServiceTest
{
    @Test
    public void testLiquibase() throws SQLException, IOException
    {
        final SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.values( SomeValue.class );

                // Set up DataSource service that will manage the connection pools
                module.services( DataSourceService.class ).identifiedBy( "datasource" );
                module.entities( DataSourceConfiguration.class );

                // Create a specific DataSource that uses the "datasource" service to do the main work
                module.importedServices( DataSource.class ).
                        importedBy( ServiceInstanceImporter.class ).
                        setMetaInfo( "datasource" ).
                        identifiedBy( "testds3" );

                // Set up Liquibase service that will create the tables
                module.services( LiquibaseService.class ).instantiateOnStartup();
                module.entities( LiquibaseConfiguration.class );
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );

                // Create in-memory store for configurations
                module.services( MemoryEntityStoreService.class );
            }
        };

        // Look up the DataSource
        DataSource ds = assembler.serviceFinder().<DataSource>findService( DataSource.class ).get();

        // Insert and query for data to check that it's working
        Databases database = new Databases( ds );

        assertTrue( database.update( "insert into test values ('someid', 'bar')" ) == 1 );

        database.query( "select * from test", new Databases.ResultSetVisitor()
        {
            @Override
            public boolean visit( ResultSet visited ) throws SQLException
            {
                assertThat( visited.getString( "id" ), equalTo( "someid" ) );
                assertThat( visited.getString( "foo" ), equalTo( "bar" ) );

                return true;
            }
        } );

        Function<ResultSet, SomeValue> toValue = new Function<ResultSet, SomeValue>()
        {
            @Override
            public SomeValue map( ResultSet resultSet )
            {
                ValueBuilder<SomeValue> builder = assembler.valueBuilderFactory().newValueBuilder( SomeValue.class );
                try
                {
                    builder.prototype().id().set( resultSet.getString( "id" ) );
                    builder.prototype().foo().set( resultSet.getString( "foo" ) );
                } catch( SQLException e )
                {
                    throw new IllegalArgumentException( "Could not convert to SomeValue", e );
                }

                return builder.newInstance();
            }
        };

        List<SomeValue> rows = new ArrayList<SomeValue>();
        database.query( "select * from test" ).transferTo( map( toValue, collection( rows ) ) );

        Inputs.iterable( rows ).transferTo( Outputs.systemOut() );

    }

    interface SomeValue
            extends ValueComposite
    {
        Property<String> id();

        Property<String> foo();
    }
}
