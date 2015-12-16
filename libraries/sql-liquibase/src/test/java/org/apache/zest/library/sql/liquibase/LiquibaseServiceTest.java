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
package org.apache.zest.library.sql.liquibase;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.junit.Test;
import org.apache.zest.api.activation.ActivationEvent;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Outputs;
import org.apache.zest.library.sql.assembly.DataSourceAssembler;
import org.apache.zest.library.sql.common.Databases;
import org.apache.zest.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.apache.zest.io.Outputs.collection;
import static org.apache.zest.io.Transforms.map;

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
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                ModuleAssembly configModule = module;
                // Create in-memory store for configurations
                new EntityTestAssembler().assemble( configModule );

                new DBCPDataSourceServiceAssembler().
                    identifiedBy( "datasource-service" ).
                    withConfig( configModule, Visibility.layer ).
                    assemble( module );
                new DataSourceAssembler().
                    withDataSourceServiceIdentity( "datasource-service" ).
                    identifiedBy( "testds-liquibase" ).
                    withCircuitBreaker().
                    assemble( module );

                module.values( SomeValue.class );

                // Set up Liquibase service that will create the tables
                // START SNIPPET: assembly
                new LiquibaseAssembler().
                    withConfig( configModule, Visibility.layer ).
                    assemble( module );
                // END SNIPPET: assembly
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().enabled().set( true );
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );

                new DefaultUnitOfWorkAssembler().assemble( module );
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
            public boolean visit( ResultSet visited )
                throws SQLException
            {
                assertThat( visited.getString( "id" ), equalTo( "someid" ) );
                assertThat( visited.getString( "foo" ), equalTo( "bar" ) );

                return true;
            }
        } );

        Function<ResultSet, SomeValue> toValue = new Function<ResultSet, SomeValue>()
        {
            @Override
            public SomeValue apply( ResultSet resultSet )
            {
                ValueBuilder<SomeValue> builder = assembler.module().newValueBuilder( SomeValue.class );
                try
                {
                    builder.prototype().id().set( resultSet.getString( "id" ) );
                    builder.prototype().foo().set( resultSet.getString( "foo" ) );
                }
                catch( SQLException e )
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
