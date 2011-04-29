/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.migration;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.map.Migration;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.assembly.EntityMigrationRule;
import org.qi4j.migration.assembly.MigrationBuilder;
import org.qi4j.migration.assembly.MigrationRule;
import org.qi4j.spi.entitystore.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public interface MigrationService
    extends Migration, Activatable, ServiceComposite
{
    public class MigrationMixin
        implements Migration, Migrator, Activatable
    {
        @Structure
        Application app;

        @This
        Configuration<MigrationConfiguration> config;

        @This
        Composite composite;

        @Service
        StateStore store;
        @Service
        EntityStore entityStore;
        @Structure
        UnitOfWorkFactory uowf;

        @This
        Migrator migrator;

        public MigrationBuilder builder;
        public Logger log;

        @Service
        Iterable<MigrationEvents> migrationEvents;

        public boolean migrate( JSONObject state, String toVersion, StateStore stateStore )
            throws JSONException
        {
            // Get current version
            String fromVersion = state.optString( MapEntityStore.JSONKeys.application_version.name(), "0.0" );

            Iterable<EntityMigrationRule> matchedRules = builder.getEntityRules().getRules( fromVersion, toVersion );

            boolean changed = false;
            if( matchedRules != null )
            {
                for( EntityMigrationRule matchedRule : matchedRules )
                {
                    boolean ruleExecuted = matchedRule.upgrade( state, stateStore, migrator );

                    if (ruleExecuted && log.isDebugEnabled())
                        log.debug( matchedRule.toString() );

                    changed = ruleExecuted || changed;
                }
            }

            state.put( MapEntityStore.JSONKeys.application_version.name(), toVersion );

            if( changed )
            {
                log.info( "Migrated " + state.getString( MapEntityStore.JSONKeys.identity.name() ) + " from " + fromVersion + " to " + toVersion );
            }

            return changed;
        }

        public void activate()
            throws Exception
        {
            builder = composite.metaInfo( MigrationBuilder.class );

            log = LoggerFactory.getLogger( MigrationService.class );

            String version = app.version();
            String lastVersion = config.configuration().lastStartupVersion().get();

            // Run general rules if version has changed
            if( !app.version().equals( lastVersion ) )
            {
                Iterable<MigrationRule> rules = builder.getRules().getRules( lastVersion, version );
                List<MigrationRule> executedRules = new ArrayList<MigrationRule>();
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

                    config.configuration().lastStartupVersion().set( version );
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

        public void passivate()
            throws Exception
        {
        }

        // Migrator implementation
        public boolean addProperty( JSONObject state, String name, Object defaultValue )
            throws JSONException
        {
            JSONObject properties = (JSONObject) state.get( MapEntityStore.JSONKeys.properties.name() );
            if( !properties.has( name ) )
            {
                if( defaultValue == null )
                {
                    properties.put( name, JSONObject.NULL );
                }
                else
                {
                    properties.put( name, defaultValue );
                }

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.propertyAdded( state.getString( MapEntityStore.JSONKeys.identity.name() ), name, defaultValue );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean removeProperty( JSONObject state, String name )
            throws JSONException
        {
            JSONObject properties = (JSONObject) state.get( MapEntityStore.JSONKeys.properties.name() );
            if( properties.has( name ) )
            {
                properties.remove( name );
                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.propertyRemoved( state.getString( MapEntityStore.JSONKeys.identity.name() ), name );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean renameProperty( JSONObject state, String from, String to )
            throws JSONException
        {
            JSONObject properties = (JSONObject) state.get( MapEntityStore.JSONKeys.properties.name() );
            if( properties.has( from ) )
            {
                Object value = properties.remove( from );
                properties.put( to, value );
                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.propertyRenamed( state.getString( MapEntityStore.JSONKeys.identity.name() ), from, to );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean addAssociation( JSONObject state, String name, String defaultReference )
            throws JSONException
        {
            JSONObject associations = (JSONObject) state.get( MapEntityStore.JSONKeys.associations.name() );
            if( !associations.has( name ) )
            {
                if( defaultReference == null )
                {
                    associations.put( name, JSONObject.NULL );
                }
                else
                {
                    associations.put( name, defaultReference );
                }

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationAdded( state.getString( MapEntityStore.JSONKeys.identity.name() ), name, defaultReference );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean removeAssociation( JSONObject state, String name )
            throws JSONException
        {
            JSONObject associations = (JSONObject) state.get( MapEntityStore.JSONKeys.associations.name() );
            if( associations.has( name ) )
            {
                associations.remove( name );
                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationRemoved( state.getString( MapEntityStore.JSONKeys.identity.name() ), name );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean renameAssociation( JSONObject state, String from, String to )
            throws JSONException
        {
            JSONObject associations = (JSONObject) state.get( MapEntityStore.JSONKeys.associations.name() );
            if( associations.has( from ) )
            {
                Object value = associations.remove( from );
                associations.put( to, value );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.associationRenamed( state.getString( MapEntityStore.JSONKeys.identity.name() ), from, to );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean addManyAssociation( JSONObject state, String name, String... defaultReferences )
            throws JSONException
        {
            JSONObject manyAssociations = (JSONObject) state.get( MapEntityStore.JSONKeys.manyassociations.name() );
            if( !manyAssociations.has( name ) )
            {
                JSONArray references = new JSONArray();
                for( String reference : defaultReferences )
                {
                    references.put( reference );
                }
                manyAssociations.put( name, references );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationAdded( state.getString( MapEntityStore.JSONKeys.identity.name() ), name, defaultReferences );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean removeManyAssociation( JSONObject state, String name )
            throws JSONException
        {
            JSONObject manyAssociations = (JSONObject) state.get( MapEntityStore.JSONKeys.manyassociations.name() );
            if( manyAssociations.has( name ) )
            {
                manyAssociations.remove( name );
                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationRemoved( state.getString( MapEntityStore.JSONKeys.identity.name() ), name );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean renameManyAssociation( JSONObject state, String from, String to )
            throws JSONException
        {
            JSONObject manyAssociations = (JSONObject) state.get( MapEntityStore.JSONKeys.manyassociations.name() );
            if( manyAssociations.has( from ) )
            {
                Object value = manyAssociations.remove( from );
                manyAssociations.put( to, value );

                for( MigrationEvents migrationEvent : migrationEvents )
                {
                    migrationEvent.manyAssociationRenamed( state.getString( MapEntityStore.JSONKeys.identity.name() ), from, to );
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public void changeEntityType( JSONObject state, String newEntityType )
            throws JSONException
        {
            state.put( MapEntityStore.JSONKeys.type.name(), newEntityType );

            for( MigrationEvents migrationEvent : migrationEvents )
            {
                migrationEvent.entityTypeChanged( state.getString( MapEntityStore.JSONKeys.identity.name() ), newEntityType );
            }
        }
    }
}
