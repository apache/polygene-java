/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.sql.liquibase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.functional.Function;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;
import static org.qi4j.io.Outputs.collection;
import static org.qi4j.io.Transforms.map;
import org.qi4j.library.sql.assembly.C3P0DataSourceServiceAssembler;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.datasource.DataSources;
import org.qi4j.library.sql.datasource.Databases;

import static org.hamcrest.CoreMatchers.equalTo;

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
                new C3P0DataSourceServiceAssembler( "datasource-service", Visibility.module, module, Visibility.module ).assemble( module );
                new DataSourceAssembler( "datasource-service",
                                         "testds3",
                                         Visibility.module,
                                         DataSources.newDataSourceCircuitBreaker() ).assemble( module );
                
                module.values( SomeValue.class );

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
        DataSource ds = assembler.module().<DataSource>findService( DataSource.class ).get();

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
                ValueBuilder<SomeValue> builder = assembler.module().newValueBuilder( SomeValue.class );
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
