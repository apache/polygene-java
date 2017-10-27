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
 */
package org.apache.polygene.entitystore.sqlkv;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TransactionProvider;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.ThreadLocalTransactionProvider;

public class SQLEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{
    private static final String IDENTITY_COLUMN_NAME = "ENTITY_IDENTITY";
    private static final String VERSION_COLUMN_NAME = "ENTITY_VERSION";
    private static final String STATE_COLUMN_NAME = "ENTITY_STATE";

    @Service
    private DataSource dataSource;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Uses
    private ServiceDescriptor descriptor;

    @This
    private Configuration<SQLEntityStoreConfiguration> configuration;

    private Table<Record> table;
    private Field<String> identityColumn;
    private Field<String> versionColumn;
    private Field<String> stateColumn;
    private DSLContext dsl;

    @Override
    public void activateService()
        throws Exception
    {
        configuration.refresh();
        SQLEntityStoreConfiguration config = configuration.get();

        // Prepare jooq DSL
        SQLDialect dialect = descriptor.metaInfo( SQLDialect.class );
        Settings settings = descriptor.metaInfo( Settings.class );
        String tableName = config.entityTableName().get();
        ConnectionProvider connectionProvider = new DataSourceConnectionProvider( dataSource );
        TransactionProvider transactionProvider = new ThreadLocalTransactionProvider( connectionProvider, false );
        org.jooq.Configuration configuration = new DefaultConfiguration()
            .set( dialect )
            .set( connectionProvider )
            .set( transactionProvider )
            .set( settings );
        dsl = DSL.using( configuration );
        table = DSL.table( DSL.name( tableName ) );
        identityColumn = DSL.field( DSL.name( IDENTITY_COLUMN_NAME ), String.class );
        versionColumn = DSL.field( DSL.name( VERSION_COLUMN_NAME ), String.class );
        stateColumn = DSL.field( DSL.name( STATE_COLUMN_NAME ), String.class );

        if( config.createIfMissing().get() )
        {
            dsl.transaction( t -> dsl.createTableIfNotExists( table )
                                     .column( identityColumn )
                                     .column( versionColumn )
                                     .column( stateColumn )
                                     .constraint( DSL.primaryKey( identityColumn ) )
                                     .execute() );
        }
    }

    @Override
    public void passivateService()
        throws Exception
    {
        dsl = null;
        table = null;
        identityColumn = null;
        versionColumn = null;
        stateColumn = null;
    }

    @Override
    public Reader get( EntityReference entityReference )
    {
        String state = dsl.select( stateColumn )
                          .from( table )
                          .where( identityColumn.equal( entityReference.identity().toString() ) )
                          .fetchOptional( stateColumn )
                          .orElseThrow( () -> new EntityNotFoundException( entityReference ) );
        return new StringReader( state );
    }

    @Override
    public Stream<Reader> entityStates()
    {
        return dsl.select( stateColumn )
                  .from( table )
                  .fetch( stateColumn )
                  .stream()
                  .map( StringReader::new );
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws Exception
    {
        List<Query> operations = new ArrayList<>();
        changes.visitMap( new MapChanger()
        {
            @Override
            public Writer newEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        String state = toString();
                        String version = jsonFactories.readerFactory().createReader( new StringReader( state ) )
                                                      .readObject()
                                                      .getString( JSONKeys.VERSION );
                        operations.add(
                            dsl.insertInto( table )
                               .columns( identityColumn, versionColumn, stateColumn )
                               .values( ref.identity().toString(), version, state )
                                      );
                    }
                };
            }

            @Override
            public Writer updateEntity( MapChange mapChange )
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        String state = toString();
                        operations.add(
                            dsl.update( table )
                               .set( versionColumn, mapChange.newVersion() )
                               .set( stateColumn, state )
                               .where( identityColumn.equal( mapChange.reference().identity().toString() ) )
                               .and( versionColumn.equal( mapChange.previousVersion() ) )
                                      );
                    }
                };
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
            {
                operations.add(
                    dsl.deleteFrom( table )
                       .where( identityColumn.equal( ref.identity().toString() ) )
                              );
            }
        } );
        dsl.transaction( t -> dsl.batch( operations ).execute() );
    }
}
