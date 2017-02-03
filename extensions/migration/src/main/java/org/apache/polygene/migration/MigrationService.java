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
package org.apache.polygene.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.migration.assembly.EntityMigrationRule;
import org.apache.polygene.migration.assembly.MigrationBuilder;
import org.apache.polygene.migration.assembly.MigrationContext;
import org.apache.polygene.migration.assembly.MigrationRule;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.Migration;
import org.apache.polygene.spi.entitystore.helpers.StateStore;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

/**
 * Migration service. This is used by MapEntityStore EntityStore implementations to
 * migrate JSON state for Entities. To use it register the service so that the EntityStore
 * can access it, and then create MigrationRules during the assembly of your application,
 * which is registered as metainfo for this service.
 *
 * The rules invoke operations, which in turn invoke the Migrator interface to perform the actual
 * migration. If the Migrator accepts a migration command the change is performed, and an event
 * is triggered. These events can be received by implementing the MigrationEvents interface in a service
 * that is visible by this MigrationService.
 */
@Mixins( MigrationService.MigrationMixin.class )
@Activators( MigrationService.Activator.class )
public interface MigrationService
    extends Migration
{
    void initialize()
        throws Exception;

    class Activator
        extends ActivatorAdapter<ServiceReference<MigrationService>>
    {
        @Override
        public void afterActivation( ServiceReference<MigrationService> activated )
            throws Exception
        {
            activated.get().initialize();
        }
    }

    class MigrationMixin
        implements MigrationService, Migrator
    {
        @Structure
        Application app;

        @This
        Configuration<MigrationConfiguration> config;

        @Uses
        ServiceDescriptor descriptor;

        @Service
        StateStore store;

        @Service
        EntityStore entityStore;

        @Service
        JsonSerialization serialization;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Migrator migrator;

        public MigrationBuilder builder;
        public Logger log;

        @Service
        Iterable<MigrationEvents> migrationEvents;

        @Override
        public JsonObject migrate( final JsonObject state, String toVersion, StateStore stateStore )
            throws JsonException
        {
            // Get current version
            String fromVersion = state.getString( JSONKeys.APPLICATION_VERSION, "0.0" );

            Iterable<EntityMigrationRule> matchedRules = builder.entityMigrationRules()
                                                                .rulesBetweenVersions( fromVersion, toVersion );

            JsonObject migratedState = state;
            boolean changed = false;
            List<String> failures = new ArrayList<>();
            if( matchedRules != null )
            {
                for( EntityMigrationRule matchedRule : matchedRules )
                {
                    MigrationContext context = new MigrationContext();

                    migratedState = matchedRule.upgrade( context, migratedState, stateStore, migrator );

                    if( context.isSuccess() && context.hasChanged() && log.isDebugEnabled() )
                    {
                        log.debug( matchedRule.toString() );
                    }

                    failures.addAll( context.failures() );
                    changed = context.hasChanged() || changed;
                }
            }

            JsonObjectBuilder appVersionBuilder = Json.createObjectBuilder();
            for( Map.Entry<String, JsonValue> entry : migratedState.entrySet() )
            {
                appVersionBuilder.add( entry.getKey(), entry.getValue() );
            }
            appVersionBuilder.add( JSONKeys.APPLICATION_VERSION, toVersion );
            migratedState = appVersionBuilder.build();

            if( failures.size() > 0 )
            {
                log.warn( "Migration of {} from {} to {} aborted, failed operation(s):\n{}",
                          state.getString( JSONKeys.IDENTITY ), fromVersion, toVersion,
                          String.join( "\n\t", failures ) );
                return state;
            }

            if( changed )
            {
                log.info( "Migrated {} from {} to {}",
                          migratedState.getString( JSONKeys.IDENTITY ), fromVersion, toVersion );
                return migratedState;
            }

            // Nothing done
            return state;
        }

        @Override
        public void initialize()
            throws Exception
        {
            builder = descriptor.metaInfo( MigrationBuilder.class );

            log = LoggerFactory.getLogger( MigrationService.class );

            String version = app.version();
            String lastVersion = config.get().lastStartupVersion().get();

            // Run general rules if version has changed
            if( !app.version().equals( lastVersion ) )
            {
                Iterable<MigrationRule> rules = builder.migrationRules().rulesBetweenVersions( lastVersion, version );
                List<MigrationRule> executedRules = new ArrayList<>();
                try
                {
                    if( rules != null )
                    {
                        for( MigrationRule rule : rules )
                        {
                            rule.upgrade( store, this );
                            executedRules.add( rule );
                            log.debug( rule.toString() );
                        }

                        log.info( "Migrated to " + version );
                    }

                    config.get().lastStartupVersion().set( version );
                    config.save();
                }
                catch( Exception e )
                {
                    log.error( "Upgrade failed", e );

                    // Downgrade the migrated rules
                    for( MigrationRule executedRule : executedRules )
                    {
                        executedRule.downgrade( store, this );
                    }
                }
            }
        }

        // Migrator implementation
        @Override
        public JsonObject addProperty( MigrationContext context, JsonObject state, String name, Object defaultValue )
            throws JsonException
        {
            JsonObject properties = state.getJsonObject( JSONKeys.PROPERTIES );
            if( !properties.containsKey( name ) )
            {
                JsonValue value = serialization.toJson( defaultValue );
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.PROPERTIES.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : properties.entrySet() )
                {
                    propBuilder.add( entry.getKey(), entry.getValue() );
                }
                propBuilder.add( name, value );
                builder.add( JSONKeys.PROPERTIES, propBuilder.build() );
                context.markAsChanged();

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.propertyAdded( state.getString( JSONKeys.IDENTITY ), name, defaultValue );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Add property " + name + ", default:" + defaultValue );
                return state;
            }
        }

        @Override
        public JsonObject removeProperty( MigrationContext context, JsonObject state, String name )
            throws JsonException
        {
            JsonObject properties = state.getJsonObject( JSONKeys.PROPERTIES );
            if( properties.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.PROPERTIES.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : properties.entrySet() )
                {
                    String key = entry.getKey();
                    if( !name.equals( key ) )
                    {
                        propBuilder.add( key, entry.getValue() );
                    }
                    else
                    {
                        context.markAsChanged();
                    }
                }
                builder.add( JSONKeys.PROPERTIES, propBuilder.build() );

                if( context.hasChanged() )
                {
                    for( MigrationEvents migrationEvent : migrationEvents )
                    {
                        migrationEvent.propertyRemoved( state.getString( JSONKeys.IDENTITY ), name );
                    }
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Remove property " + name );
                return state;
            }
        }

        @Override
        public JsonObject renameProperty( MigrationContext context, JsonObject state, String from, String to )
            throws JsonException
        {
            JsonObject properties = state.getJsonObject( JSONKeys.PROPERTIES );
            if( properties.containsKey( from ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.PROPERTIES.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder propBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : properties.entrySet() )
                {
                    String key = entry.getKey();
                    if( from.equals( key ) )
                    {
                        propBuilder.add( to, entry.getValue() );
                        context.markAsChanged();
                    }
                    else
                    {
                        propBuilder.add( key, entry.getValue() );
                    }
                }
                builder.add( JSONKeys.PROPERTIES, propBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.propertyRenamed( state.getString( JSONKeys.IDENTITY ), from, to );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Rename property " + from + " to " + to );
                return state;
            }
        }

        @Override
        public JsonObject addAssociation( MigrationContext context, JsonObject state, String name,
                                          String defaultReference )
            throws JsonException
        {
            JsonObject associations = state.getJsonObject( JSONKeys.ASSOCIATIONS );
            if( !associations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : associations.entrySet() )
                {
                    assocBuilder.add( entry.getKey(), entry.getValue() );
                }
                JsonValue value = serialization.toJson( defaultReference );
                assocBuilder.add( name, value );
                builder.add( JSONKeys.ASSOCIATIONS, assocBuilder.build() );
                context.markAsChanged();

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationAdded( state.getString( JSONKeys.IDENTITY ), name, defaultReference );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Add association " + name + ", default:" + defaultReference );
                return state;
            }
        }

        @Override
        public JsonObject removeAssociation( MigrationContext context, JsonObject state, String name )
            throws JsonException
        {
            JsonObject associations = state.getJsonObject( JSONKeys.ASSOCIATIONS );
            if( associations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : associations.entrySet() )
                {
                    String key = entry.getKey();
                    if( !name.equals( key ) )
                    {
                        assocBuilder.add( key, entry.getValue() );
                    }
                    else
                    {
                        context.markAsChanged();
                    }
                }
                builder.add( JSONKeys.ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationRemoved( state.getString( JSONKeys.IDENTITY ), name );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Remove association " + name );
                return state;
            }
        }

        @Override
        public JsonObject renameAssociation( MigrationContext context, JsonObject state, String from, String to )
            throws JsonException
        {
            JsonObject associations = state.getJsonObject( JSONKeys.ASSOCIATIONS );
            if( associations.containsKey( from ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : associations.entrySet() )
                {
                    String key = entry.getKey();
                    if( from.equals( key ) )
                    {
                        assocBuilder.add( to, entry.getValue() );
                        context.markAsChanged();
                    }
                    else
                    {
                        assocBuilder.add( to, entry.getValue() );
                    }
                }
                builder.add( JSONKeys.ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationRenamed( state.getString( JSONKeys.IDENTITY ), from, to );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Rename association " + from + " to " + to );
                return state;
            }
        }

        @Override
        public JsonObject addManyAssociation( MigrationContext context, JsonObject state, String name,
                                              String... defaultReferences )
            throws JsonException
        {
            JsonObject manyAssociations = state.getJsonObject( JSONKeys.MANY_ASSOCIATIONS );
            if( !manyAssociations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.MANY_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : manyAssociations.entrySet() )
                {
                    assocBuilder.add( entry.getKey(), entry.getValue() );
                }
                JsonValue value = serialization.toJson( defaultReferences );
                assocBuilder.add( name, value );
                builder.add( JSONKeys.MANY_ASSOCIATIONS, assocBuilder.build() );
                context.markAsChanged();

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationAdded( state.getString( JSONKeys.IDENTITY ), name,
                                                         defaultReferences );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Add many-association " + name + ", default:" + asList( defaultReferences ) );
                return state;
            }
        }

        @Override
        public JsonObject removeManyAssociation( MigrationContext context, JsonObject state, String name )
            throws JsonException
        {
            JsonObject manyAssociations = state.getJsonObject( JSONKeys.MANY_ASSOCIATIONS );
            if( manyAssociations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.MANY_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : manyAssociations.entrySet() )
                {
                    String key = entry.getKey();
                    if( !name.equals( key ) )
                    {
                        assocBuilder.add( key, entry.getValue() );
                    }
                    else
                    {
                        context.markAsChanged();
                    }
                }
                builder.add( JSONKeys.MANY_ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationRemoved( state.getString( JSONKeys.IDENTITY ), name );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Remove many-association " + name );
                return state;
            }
        }

        @Override
        public JsonObject renameManyAssociation( MigrationContext context, JsonObject state, String from, String to )
            throws JsonException
        {
            JsonObject manyAssociations = state.getJsonObject( JSONKeys.MANY_ASSOCIATIONS );
            if( manyAssociations.containsKey( from ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.MANY_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : manyAssociations.entrySet() )
                {
                    String key = entry.getKey();
                    if( from.equals( key ) )
                    {
                        context.markAsChanged();
                        assocBuilder.add( to, entry.getValue() );
                    }
                    else
                    {
                        assocBuilder.add( key, entry.getValue() );
                    }
                }
                builder.add( JSONKeys.MANY_ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationRenamed( state.getString( JSONKeys.IDENTITY ), from, to );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Rename many-association " + from + " to " + to );
                return state;
            }
        }

        @Override
        public JsonObject addNamedAssociation( MigrationContext context, JsonObject state, String name,
                                               Map<String, String> defaultReferences )
            throws JsonException
        {
            JsonObject namedAssociations = state.getJsonObject( JSONKeys.NAMED_ASSOCIATIONS );
            if( !namedAssociations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.NAMED_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : namedAssociations.entrySet() )
                {
                    assocBuilder.add( entry.getKey(), entry.getValue() );
                }
                JsonValue value = serialization.toJson( defaultReferences );
                assocBuilder.add( name, value );
                builder.add( JSONKeys.NAMED_ASSOCIATIONS, assocBuilder.build() );
                context.markAsChanged();

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.namedAssociationAdded( state.getString( JSONKeys.IDENTITY ), name,
                                                          defaultReferences );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Add named-association " + name + ", default:" + defaultReferences );
                return state;
            }
        }

        @Override
        public JsonObject removeNamedAssociation( MigrationContext context, JsonObject state, String name )
            throws JsonException
        {
            JsonObject namedAssociations = state.getJsonObject( JSONKeys.NAMED_ASSOCIATIONS );
            if( namedAssociations.containsKey( name ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.NAMED_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : namedAssociations.entrySet() )
                {
                    String key = entry.getKey();
                    if( !name.equals( key ) )
                    {
                        assocBuilder.add( key, entry.getValue() );
                    }
                    else
                    {
                        context.markAsChanged();
                    }
                }
                builder.add( JSONKeys.NAMED_ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.namedAssociationRemoved( state.getString( JSONKeys.IDENTITY ), name );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Remove named-association " + name );
                return state;
            }
        }

        @Override
        public JsonObject renameNamedAssociation( MigrationContext context, JsonObject state, String from, String to )
            throws JsonException
        {
            JsonObject namedAssociations = state.getJsonObject( JSONKeys.NAMED_ASSOCIATIONS );
            if( namedAssociations.containsKey( from ) )
            {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : state.entrySet() )
                {
                    String key = entry.getKey();
                    if( !JSONKeys.NAMED_ASSOCIATIONS.equals( key ) )
                    {
                        builder.add( key, entry.getValue() );
                    }
                }
                JsonObjectBuilder assocBuilder = Json.createObjectBuilder();
                for( Map.Entry<String, JsonValue> entry : namedAssociations.entrySet() )
                {
                    String key = entry.getKey();
                    if( from.equals( key ) )
                    {
                        assocBuilder.add( to, entry.getValue() );
                        context.markAsChanged();
                    }
                    else
                    {
                        assocBuilder.add( key, entry.getValue() );
                    }
                }
                builder.add( JSONKeys.NAMED_ASSOCIATIONS, assocBuilder.build() );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.namedAssociationRenamed( state.getString( JSONKeys.IDENTITY ), from, to );
                }

                return builder.build();
            }
            else
            {
                context.addFailure( "Rename named-association " + from + " to " + to );
                return state;
            }
        }

        @Override
        public JsonObject changeEntityType( MigrationContext context, JsonObject state,
                                            String fromType, String toType )
            throws JsonException
        {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for( Map.Entry<String, JsonValue> entry : state.entrySet() )
            {
                String key = entry.getKey();
                if( JSONKeys.TYPE.equals( key ) )
                {
                    String oldValue = entry.getValue().getValueType() == JsonValue.ValueType.STRING
                                      ? ( (JsonString) entry.getValue() ).getString()
                                      : entry.getValue().toString();
                    if( !fromType.equals( oldValue ) )
                    {
                        context.addFailure( "Change entity type from " + fromType + " to " + toType );
                        return state;
                    }
                    builder.add( JSONKeys.TYPE, toType );
                    context.markAsChanged();
                }
                else
                {
                    builder.add( key, entry.getValue() );
                }
            }

            for( MigrationEvents migrationEvent : migrationEvents )
            {
                migrationEvent.entityTypeChanged( state.getString( JSONKeys.IDENTITY ), toType );
            }

            return builder.build();
        }
    }
}
