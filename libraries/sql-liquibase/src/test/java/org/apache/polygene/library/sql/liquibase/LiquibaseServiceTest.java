/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.sql.liquibase;

import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.apache.polygene.library.sql.assembly.DataSourceAssembler;
import org.apache.polygene.library.sql.dbcp.DBCPDataSourceServiceAssembler;
import org.apache.polygene.test.EntityTestAssembler;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStep2;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test DataSource and Liquibase services
 */
public class LiquibaseServiceTest
{
    @Test
    public void testLiquibase() throws ActivationException
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

                new DBCPDataSourceServiceAssembler()
                    .identifiedBy( "datasource-service" )
                    .withConfig( configModule, Visibility.layer )
                    .assemble( module );
                new DataSourceAssembler()
                    .withDataSourceServiceIdentity( "datasource-service" )
                    .identifiedBy( "testds-liquibase" )
                    .withCircuitBreaker()
                    .assemble( module );

                module.values( SomeValue.class );

                // Set up Liquibase service that will create the tables
                // START SNIPPET: assembly
                new LiquibaseAssembler()
                    .withConfig( configModule, Visibility.layer )
                    .applyChangelogOnStartup()
                    .assemble( module );
                // END SNIPPET: assembly
                module.forMixin( LiquibaseConfiguration.class ).declareDefaults().changeLog().set( "changelog.xml" );
            }

            @Override
            public void beforeActivation( Application application )
            {
                application.registerActivationEventListener( System.out::println );
            }
        };

        Module module = assembler.module();

        // Look up the DataSource
        DataSource ds = module.findService( DataSource.class ).get();

        // Prepare jOOQ and the schema model
        DSLContext jooq = DSL.using( ds, SQLDialect.DERBY );
        Table<Record> testTable = table( "TEST" );
        Field<String> idColumn = field( "ID", String.class );
        Field<String> fooColumn = field( "FOO", String.class );

        // Assert that insertion works
        InsertValuesStep2 insert = jooq.insertInto( testTable )
                                       .columns( idColumn, fooColumn )
                                       .values( "someid", "bar" );
        assertTrue( insert.execute() == 1 );

        List<Record> records = jooq.selectFrom( testTable ).stream().collect( toList() );
        assertThat( records.size(), is( 1 ) );
        assertThat( records.get( 0 ).get( idColumn ), equalTo( "someid" ) );
        assertThat( records.get( 0 ).get( fooColumn ), equalTo( "bar" ) );

        Function<Record, SomeValue> toValue = record ->
        {
            ValueBuilder<SomeValue> builder = assembler.module().newValueBuilder( SomeValue.class );
            builder.prototype().id().set( record.get( idColumn ) );
            builder.prototype().foo().set( record.get( fooColumn ) );
            return builder.newInstance();
        };

        List<SomeValue> values = jooq.selectFrom( testTable ).stream()
                                     .map( toValue )
                                     .peek( System.out::println )
                                     .collect( toList() );

        assertThat( values.size(), is( 1 ) );
        assertThat( values.get( 0 ).id().get(), equalTo( "someid" ) );
        assertThat( values.get( 0 ).foo().get(), equalTo( "bar" ) );
    }

    interface SomeValue
        extends ValueComposite
    {
        Property<String> id();

        Property<String> foo();
    }
}
