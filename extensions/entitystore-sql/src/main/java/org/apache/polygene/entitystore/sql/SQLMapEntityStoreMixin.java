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
package org.apache.polygene.entitystore.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.library.sql.liquibase.LiquibaseService;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

public class SQLMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{
    private static final String TABLE_NAME_LIQUIBASE_PARAMETER = "es-sql.table";
    private static final String IDENTITY_COLUMN_NAME = "ENTITY_IDENTITY";
    private static final String VERSION_COLUMN_NAME = "ENTITY_VERSION";
    private static final String STATE_COLUMN_NAME = "ENTITY_STATE";

    @Service
    private DataSource dataSource;

    @Service
    private LiquibaseService liquibaseService;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Uses
    private ServiceDescriptor descriptor;

    @This
    private Configuration<SQLMapEntityStoreConfiguration> configuration;

    private Schema schema;
    private Table<Record> table;
    private Field<String> identityColumn;
    private Field<String> versionColumn;
    private Field<String> stateColumn;
    private DSLContext dsl;

    @Override
    public void activateService() throws Exception
    {
        configuration.refresh();
        SQLMapEntityStoreConfiguration config = configuration.get();

        // Prepare jooq DSL
        SQLDialect dialect = descriptor.metaInfo( SQLDialect.class );
        Settings settings = descriptor.metaInfo( Settings.class );
        String schemaName = config.schemaName().get();
        String tableName = config.entityTableName().get();
        schema = DSL.schema( DSL.name( schemaName ) );
        table = DSL.table(
            dialect.equals( SQLDialect.SQLITE )
            ? DSL.name( tableName )
            : DSL.name( schema.getName(), tableName )
        );
        identityColumn = DSL.field( DSL.name( IDENTITY_COLUMN_NAME ), String.class );
        versionColumn = DSL.field( DSL.name( VERSION_COLUMN_NAME ), String.class );
        stateColumn = DSL.field( DSL.name( STATE_COLUMN_NAME ), String.class );
        dsl = DSL.using( dataSource, dialect, settings );

        // Eventually create schema and apply Liquibase changelog
        if( config.createIfMissing().get() )
        {
            if( !dialect.equals( SQLDialect.SQLITE )
                && dsl.meta().getSchemas().stream().noneMatch( s -> schema.getName().equalsIgnoreCase( s.getName() ) ) )
            {
                dsl.createSchema( schema ).execute();
            }

            applyLiquibaseChangelog( dialect );
        }
    }

    private void applyLiquibaseChangelog( SQLDialect dialect ) throws SQLException, LiquibaseException
    {
        Liquibase liquibase = liquibaseService.newConnectedLiquibase();
        Database db = liquibase.getDatabase();
        db.setObjectQuotingStrategy( ObjectQuotingStrategy.QUOTE_ALL_OBJECTS );
        try
        {
            if( !dialect.equals( SQLDialect.SQLITE ) )
            {
                if( db.supportsSchemas() )
                {
                    db.setDefaultSchemaName( schema.getName() );
                    db.setLiquibaseSchemaName( schema.getName() );
                }
                if( db.supportsCatalogs() )
                {
                    db.setDefaultCatalogName( schema.getName() );
                    db.setLiquibaseCatalogName( schema.getName() );
                }
            }
            liquibase.getChangeLogParameters().set( TABLE_NAME_LIQUIBASE_PARAMETER, table.getName() );
            liquibase.update( new Contexts() );
        }
        finally
        {
            db.close();
        }
    }

    @Override
    public void passivateService() throws Exception
    {
        dsl = null;
        schema = null;
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
    public void applyChanges( MapChanges changes ) throws Exception
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
                    public void close() throws IOException
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
                    public void close() throws IOException
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
        dsl.batch( operations ).execute();
    }
}
