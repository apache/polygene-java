/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;

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
        ps.setString( 1, ref.identity() );
    }

    @Override
    public void populateInsertEntityStatement( PreparedStatement ps, EntityReference ref, String entity, Long lastModified )
            throws SQLException
    {
        ps.setString( 1, ref.identity() );
        ps.setString( 2, entity );
        ps.setLong( 3, lastModified );
    }

    @Override
    public void populateRemoveEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref )
            throws SQLException
    {
        ps.setLong( 1, entityPK );
    }

    @Override
    public void populateUpdateEntityStatement( PreparedStatement ps, Long entityPK, Long entityOptimisticLock, EntityReference ref, String entity, Long lastModified )
            throws SQLException
    {
        ps.setLong( 1, entityOptimisticLock + 1 );
        ps.setString( 2, entity );
        ps.setLong( 3, lastModified );
        ps.setLong( 4, entityPK );
        ps.setLong( 5, entityOptimisticLock );
    }

}
