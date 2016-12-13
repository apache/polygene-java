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

package org.apache.polygene.migration.operation;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.polygene.migration.Migrator;
import org.apache.polygene.migration.assembly.EntityMigrationOperation;
import org.apache.polygene.spi.entitystore.helpers.StateStore;

/**
 * Rename an Association
 */
public class RenameAssociation
    implements EntityMigrationOperation
{
    String from;
    String to;

    public RenameAssociation( String from, String to )
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean upgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        return migrator.renameAssociation( state, from, to );
    }

    @Override
    public boolean downgrade( JSONObject state, StateStore stateStore, Migrator migrator )
        throws JSONException
    {
        return migrator.renameAssociation( state, to, from );
    }

    @Override
    public String toString()
    {
        return "Rename association " + from + " to " + to;
    }
}
