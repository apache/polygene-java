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

package org.qi4j.migration.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.util.ListMap;

/**
 * Set of migration rules.
 */
public class MigrationRules<T extends AbstractMigrationRule>
{
    // to-version -> List of from-versions
    ListMap<String, String> versionChanges = new ListMap<String, String>();

    // key=fromversion->toversion value=list of rules for that transition
    ListMap<String, T> rules = new ListMap<String, T>();

    public void addRule( T migrationRule )
    {
        versionChanges.add( migrationRule.toVersion(), migrationRule.fromVersion() );
        rules.add( migrationRule.fromVersion() + "->" + migrationRule.toVersion(), migrationRule );
    }

    public Iterable<T> rulesBetweenVersions( String fromVersion, String toVersion )
    {
        try
        {
            String ruleToVersion = findHighestToVersion( toVersion );

            return migrationRules( fromVersion, ruleToVersion );
        }
        catch( IllegalArgumentException e )
        {
            return null;
        }
    }

    private List<T> migrationRules( String fromVersion, String toVersion )
    {
        List<String> list = versionChanges.get( toVersion );

        if( list == null )
        {
            return null; // No possible rules for this transition
        }

        for( String possibleFromVersion : list )
        {
            if( fromVersion.equals( possibleFromVersion ) )
            {
                // We found the end of the version transitions - return rules, but filter on entity type
                return new ArrayList<T>( getRulesForTransition( fromVersion, toVersion ) );
            }
            else
            {
                List<T> migrationRules = migrationRules( fromVersion, possibleFromVersion );
                if( migrationRules == null )
                {
                    continue; // Wrong transition - try another one
                }

                // Add entity-filtered rules from this part of the version transition
                migrationRules.addAll( getRulesForTransition( possibleFromVersion, toVersion ) );
                return migrationRules;
            }
        }
        return null;
    }

    /**
     * Find highest version below the given to-version for which there are rules registered.
     *
     * @param toVersion The ending toVersion in the search.
     *
     * @return The highest version below the given version for which there are rules registered.
     */
    private String findHighestToVersion( String toVersion )
    {
        if( versionChanges.get( toVersion ) == null )
        {
            List<String> toVersions = new ArrayList<String>( versionChanges.keySet() );
            Collections.sort( toVersions, Collections.reverseOrder() );
            for( String version : toVersions )
            {
                if( version.compareTo( toVersion ) <= 0 )
                {
                    // Found version to change to
                    return version;
                }
            }
            throw new IllegalArgumentException( "No version found in rules that matches the given to-version:" + toVersion );
        }
        else
        {
            return toVersion;
        }
    }

    private List<T> getRulesForTransition( String fromVersion, String toVersion )
    {
        return rules.get( fromVersion + "->" + toVersion );
    }
}
