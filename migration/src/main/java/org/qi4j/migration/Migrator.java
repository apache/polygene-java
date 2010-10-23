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
import org.qi4j.api.common.QualifiedName;

/**
 * The Migrator implements this interface, which is invoked by MigrationOperation implementations to perform changes to
 * EntityState during a version migration.
 */
public interface Migrator
{
    boolean addProperty( JSONObject state, QualifiedName name, String defaultValue )
        throws JSONException;

    boolean removeProperty( JSONObject state, QualifiedName name )
        throws JSONException;

    boolean renameProperty( JSONObject state, QualifiedName from, QualifiedName to )
        throws JSONException;

    boolean addAssociation( JSONObject state, QualifiedName name, String defaultReference )
        throws JSONException;

    boolean removeAssociation( JSONObject state, QualifiedName name )
        throws JSONException;

    boolean renameAssociation( JSONObject state, QualifiedName from, QualifiedName to )
        throws JSONException;

    boolean addManyAssociation( JSONObject state, QualifiedName name, String... defaultReference )
        throws JSONException;

    boolean removeManyAssociation( JSONObject state, QualifiedName name )
        throws JSONException;

    boolean renameManyAssociation( JSONObject state, QualifiedName from, QualifiedName to )
        throws JSONException;

    void changeEntityType( JSONObject state, String newEntityType )
        throws JSONException;
}
