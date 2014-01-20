/*
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.migration.operation;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.migration.Migrator;
import org.qi4j.migration.assembly.EntityMigrationOperation;
import org.qi4j.spi.entitystore.helpers.StateStore;

/**
 * Add a named-association
 */
public class RemoveNamedAssociation
    implements EntityMigrationOperation
{
    private final String association;
    private final Map<String, String> defaultReferences;

    public RemoveNamedAssociation( String association, Map<String, String> defaultReferences )
    {
        this.association = association;
        this.defaultReferences = defaultReferences;
    }

    @Override
    public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        return migrator.removeNamedAssociation( state, association );
    }

    @Override
    public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        return migrator.addNamedAssociation( state, association, defaultReferences );
    }

    @Override
    public String toString()
    {
        return "Remove named-association " + association + ", default:" + defaultReferences;
    }
}
