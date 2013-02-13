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
import org.junit.Test;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.functional.Function;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;
import org.qi4j.library.sql.assembly.DataSourceAssembler;
import org.qi4j.library.sql.c3p0.C3P0DataSourceServiceAssembler;
import org.qi4j.library.sql.common.Databases;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.qi4j.io.Outputs.collection;
import static org.qi4j.io.Transforms.map;

/**
 * Test DataSource and Liquibase services
 */
public class LiquibaseServiceTest
{
    @Test
    public void testLiquibase()
        throws SQLException, IOException, ActivationException, AssemblyException
    {
        final SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                new C3P0DataSourceServiceAssembler().
                        identifiedBy( "datasource-service" ).
                        assemble( module );
                new DataSourceAssembler().
                        withDataSourceServiceIdentity( "datasource-service").
                        identifiedBy( "testds-liquibase").
                        withCircuitBreaker().
                        assemble( module );

                module.values( SomeValue.class );

                // Set up Liquibase service that will create the tables
                ModuleAssembly configModule = module;
                // START SNIPPET: assembly
                new LiquibaseAssembler( Visibility.module ).
                        withConfigIn( configModule, Visibility.layer ).
                        assemble( module );
                // END SNIPPET: assembly
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );

                // Create in-memory store for configurations
                new EntityTestAssembler().assemble( module );
            }

            @Override
            public void beforeActivation( Application application )
            {
                application.registerActivationEventListener( new ActivationEventListener()
                {

                    @Override
                    public void onEvent( ActivationEvent event )
                    {
                        System.out.println( event );
                    }

                } );
            }

        };

        Module module = assembler.module();

        // START SNIPPET: io
        // Look up the DataSource
        DataSource ds = module.findService( DataSource.class ).get();

        // Instanciate Databases helper
        Databases database = new Databases( ds );

        // Assert that insertion works
        assertTrue( database.update( "insert into test values ('someid', 'bar')" ) == 1 );
        // END SNIPPET: io

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

        // START SNIPPET: io
        // Select rows and load them in a List
        List<SomeValue> rows = new ArrayList<SomeValue>();
        database.query( "select * from test" ).transferTo( map( toValue, collection( rows ) ) );

        // Transfer all rows to System.out
        Inputs.iterable( rows ).transferTo( Outputs.systemOut() );
        // END SNIPPET: io
    }

    interface SomeValue
            extends ValueComposite
    {
        Property<String> id();

        Property<String> foo();
    }
}
