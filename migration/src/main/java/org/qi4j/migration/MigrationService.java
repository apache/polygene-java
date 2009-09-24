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

import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.map.Migration;
import org.qi4j.entitystore.map.StateStore;
import org.qi4j.migration.assembly.MigrationRule;
import org.qi4j.migration.assembly.MigrationRules;

/**
 * Migration service. This is used by MapEntityStore EntityStore implementations to
 * migrate JSON state for Entities. To use it register the service so that the EntityStore
 * can access it, and then create MigrationRules during the assembly of your application,
 * which is registered as metainfo for this service.
 */
@Mixins( MigrationService.MigrationMixin.class )
public interface MigrationService
    extends Migration, Activatable, ServiceComposite
{
    class MigrationMixin
        implements Migration, Activatable
    {
        @This
        Composite composite;
        public MigrationRules rules;

        public boolean migrate( JSONObject state, String toVersion, StateStore stateStore ) throws JSONException
        {
            // Get current version
            String fromVersion = state.getString( MapEntityStore.JSONKeys.application_version.name() );

            Iterable<MigrationRule> matchedRules = rules.getRules( fromVersion, toVersion );

            boolean changed = false;
            for( MigrationRule matchedRule : matchedRules )
            {
                changed = matchedRule.upgrade( state, stateStore ) || changed;
            }

            state.put( MapEntityStore.JSONKeys.application_version.name(), toVersion );

            return changed;
        }

        public void activate() throws Exception
        {
            rules = composite.metaInfo( MigrationRules.class );
        }

        public void passivate() throws Exception
        {
        }
    }
}
