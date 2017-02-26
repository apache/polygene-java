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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.json.Json;
import javax.sql.DataSource;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.library.sql.common.SQLConfiguration;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Implement optimistic locking! Maybe as a SPI helper (in-progress)
// TODO Add schema version data into the DB, check it
// TODO Remove old SQL ES Code
public class SQLMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SQLMapEntityStoreService.class );

    @Service
    private DataSource dataSource;

    @Uses
    private ServiceDescriptor descriptor;

    @This
    @Optional
    private Configuration<SQLConfiguration> configuration;

    private Schema schema;
    private Table<Record> table;
    private Field<String> identityColumn;
    private Field<String> versionColumn;
    private Field<String> stateColumn;
    private DSLContext dsl;

    @Override
    public void activateService() throws Exception
    {
        SQLDialect dialect = descriptor.metaInfo( SQLDialect.class );
        Settings settings = descriptor.metaInfo( Settings.class );
        SQLMapEntityStoreMapping mapping = descriptor.metaInfo( SQLMapEntityStoreMapping.class );
        String schemaName = getConfiguredSchemaName( mapping.defaultSchemaName() );
        if( schemaName == null )
        {
            throw new EntityStoreException( "Schema name must not be null." );
        }
        schema = DSL.schema( DSL.name( schemaName.toUpperCase() ) );
        table = DSL.table(
            dialect.equals( SQLDialect.SQLITE )
            ? DSL.name( mapping.tableName() )
            : DSL.name( schema.getName(), mapping.tableName() )
        );
        identityColumn = DSL.field( mapping.identityColumnName(), String.class );
        versionColumn = DSL.field( mapping.versionColumnName(), String.class );
        stateColumn = DSL.field( mapping.stateColumnName(), String.class );

        dsl = DSL.using( dataSource, dialect, settings );

        if( !dialect.equals( SQLDialect.SQLITE )
            && dsl.meta().getSchemas().stream().noneMatch( s -> schema.getName().equals( s.getName() ) ) )
        {
            dsl.createSchema( schema ).execute();
        }

        if( dsl.meta().getTables().stream().noneMatch( t -> table.getName().equals( t.getName() ) ) )
        {
            dsl.createTable( table )
               .column( identityColumn, mapping.identityDataType().nullable( false ) )
               .column( versionColumn, mapping.versionDataType().nullable( false ) )
               .column( stateColumn, mapping.stateDataType().nullable( false ) )
               .constraint( DSL.constraint( "ENTITY_IDENTITY_CONSTRAINT" ).primaryKey( identityColumn ) )
               .execute();
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
                        String version = Json.createReader( new StringReader( state ) ).readObject()
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


    /**
     * Configuration is optional at both assembly and runtime.
     */
    protected String getConfiguredSchemaName( String defaultSchemaName )
    {
        if( configuration == null )
        {
            Objects.requireNonNull( defaultSchemaName, "default schema name" );
            LOGGER.debug( "No configuration, will use default schema name: '{}'", defaultSchemaName );
            return defaultSchemaName;
        }
        String result = configuration.get().schemaName().get();
        if( result == null )
        {
            Objects.requireNonNull( defaultSchemaName, "default schema name" );
            result = defaultSchemaName;
            LOGGER.debug( "No database schema name in configuration, will use default: '{}'", defaultSchemaName );
        }
        else
        {
            LOGGER.debug( "Will use configured database schema name: '{}'", result );
        }
        return result;
    }
}
