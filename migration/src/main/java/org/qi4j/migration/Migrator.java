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

/**
 * The Migrator implements this interface, which is invoked by MigrationOperation implementations
 * to perform changes to EntityState during a version migration.
 */
public interface Migrator
{
    boolean addProperty( JSONObject state, String name, Object defaultValue )
        throws JSONException;

    boolean removeProperty( JSONObject state, String name )
        throws JSONException;

    boolean renameProperty( JSONObject state, String from, String to )
        throws JSONException;

    boolean addAssociation( JSONObject state, String name, String defaultReference )
        throws JSONException;

    boolean removeAssociation( JSONObject state, String name )
        throws JSONException;

    boolean renameAssociation( JSONObject state, String from, String to )
        throws JSONException;

    boolean addManyAssociation( JSONObject state, String name, String... defaultReference )
        throws JSONException;

    boolean removeManyAssociation( JSONObject state, String name )
        throws JSONException;

    boolean renameManyAssociation( JSONObject state, String from, String to )
        throws JSONException;

    void changeEntityType( JSONObject state, String newEntityType )
        throws JSONException;
}
