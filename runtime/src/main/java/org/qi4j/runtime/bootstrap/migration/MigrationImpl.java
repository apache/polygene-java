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

package org.qi4j.runtime.bootstrap.migration;

import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
public class MigrationImpl
{
    Map<String, Map<String, MigrationRules>> rules;
    private MigrationRules defaultRules;

    public MigrationImpl()
    {
        rules = new HashMap<String, Map<String, MigrationRules>>();
        defaultRules = new MigrationRules();
    }

    public void addRule(String fromVersion, String toVersion, MigrationRule rule)
    {
        if (fromVersion == null && toVersion == null)
            defaultRules.addRule(rule);
        else
        {
            Map<String, MigrationRules> fromRules = rules.get(fromVersion);
            if (fromRules == null)
            {
                fromRules = new HashMap<String, MigrationRules>();
                rules.put(fromVersion, fromRules);
            }
            MigrationRules toRules = fromRules.get(toVersion);
            if (toRules == null)
            {
                toRules = new MigrationRules();
                fromRules.put(toVersion, toRules);
            }
            toRules.addRule(rule);
        }

    }

    public void migrate(EntityState state, EntityType fromType, EntityType toType)
    {
        MigrationRules migrationRules = getRules(fromType, toType);

        if (migrationRules == null)
            migrationRules = defaultRules;

        migrationRules.migrate(state,  fromType, toType);
    }

    private MigrationRules getRules(EntityType fromType, EntityType toType)
    {
        Map<String, MigrationRules> fromRules = rules.get(fromType.version());
        if (fromRules == null)
        {
            return null;
        } else
        {
            return fromRules.get(toType.version());
        }
    }
}
