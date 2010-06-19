/*
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
package org.qi4j.entitystore.sql.database;

import org.qi4j.entitystore.sql.util.SQLUtil;
import java.sql.PreparedStatement;
import org.qi4j.api.configuration.Configuration;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import static org.qi4j.entitystore.sql.util.SQLUtil.*;

public abstract class AbstractDatabaseService
        implements DatabaseService
{

    protected final Configuration<DatabaseConfiguration> cfg;

    public AbstractDatabaseService( Configuration<DatabaseConfiguration> cfg )
    {
        this.cfg = cfg;
    }

    public void startDatabase()
            throws Exception
    {
        Class.forName( ensureConfiguration( cfg ).driver().get() );
        initDatabase();
    }

    public void stopDatabase()
            throws Exception
    {
        shutdownDatabase();
    }

    public Connection openConnection()
            throws SQLException
    {
        Connection connection = DriverManager.getConnection( buildConnectionString( cfg ),
                                                             ensureConfiguration( cfg ).user().get(),
                                                             ensureConfiguration( cfg ).password().get() );
        connection.setAutoCommit( false );
        return connection;
    }

    protected abstract void initDatabase();

    protected void shutdownDatabase()
            throws SQLException
    {
        // NOOP
    }

    public PreparedStatement prepareGetEntityStatement( Connection connection, String identity )
            throws SQLException
    {
        PreparedStatement select = connection.prepareStatement( SQLUtil.SELECT_STATE_SQL );
        select.setString( 1, identity );
        return select;
    }

    public PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
            throws SQLException
    {
        return connection.prepareStatement( SQLUtil.SELECT_ALL_STATES_SQL );
    }

    public PreparedStatement prepareInsertEntityStatement( Connection connection, String identity, String value )
            throws SQLException
    {
        PreparedStatement insertNewEntity = connection.prepareStatement( SQLUtil.INSERT_STATE_SQL );
        insertNewEntity.setString( 1, identity );
        insertNewEntity.setString( 2, value );
        return insertNewEntity;
    }

    public PreparedStatement prepareUpdateEntityStatement( Connection connection, String identity, String value )
            throws SQLException
    {
        PreparedStatement updateEntity = connection.prepareStatement( SQLUtil.UPDATE_STATE_SQL );
        updateEntity.setString( 1, value );
        updateEntity.setString( 2, identity );
        return updateEntity;
    }

    public PreparedStatement prepareRemoveEntityStatement( Connection connection, String identity )
            throws SQLException
    {
        PreparedStatement removeEntity = connection.prepareStatement( SQLUtil.REMOVE_STATE_SQL );
        removeEntity.setString( 1, identity );
        return removeEntity;
    }

}
