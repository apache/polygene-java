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
package org.apache.polygene.entitystore.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;

public abstract class DatabaseSQLServiceStatementsMixin
        implements DatabaseSQLService
{

    @This
    private DatabaseSQLStringsBuilder sqlStrings;

    //
    // Used by the EntityStore, will probably remain the same even if we support several sql servers
    //
    @Override
    public PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( sqlStrings.buildSQLForSelectAllEntitiesStatement() );
    }

    @Override
    public PreparedStatement prepareGetEntityStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( sqlStrings.buildSQLForSelectEntityStatement() );
    }

    @Override
    public PreparedStatement prepareInsertEntityStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( sqlStrings.buildSQLForInsertEntityStatement() );
    }

    @Override
    public PreparedStatement prepareRemoveEntityStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( sqlStrings.buildSQLForRemoveEntityStatement() );
    }

    @Override
    public PreparedStatement prepareUpdateEntityStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( sqlStrings.buildSQLForUpdateEntityStatement() );
    }

    //
    // Populate statement methods, to move in a separated fragment if needed for multi sql server support
    //
    @Override
    public void populateGetAllEntitiesStatement( PreparedStatement ps )
            throws SQLException
    {
        // Nothing to do.
    }

    @Override
    public void populateGetEntityStatement( PreparedStatement ps, EntityReference ref )
            throws SQLException
    {
        ps.setString( 1, ref.identity().toString() );
    }

    @Override
    public void populateInsertEntityStatement( PreparedStatement ps, EntityReference ref, String entity )
            throws SQLException
    {
        ps.setString( 1, ref.identity().toString() );
        ps.setString( 2, entity );
    }

    @Override
    public void populateRemoveEntityStatement( PreparedStatement ps, EntityReference ref )
            throws SQLException
    {
        ps.setString( 1, ref.identity().toString() );
    }

    @Override
    public void populateUpdateEntityStatement( PreparedStatement ps, EntityReference ref, String entity )
            throws SQLException
    {
        ps.setString( 1, ref.identity().toString() );
        ps.setString( 2, entity );
    }

}
