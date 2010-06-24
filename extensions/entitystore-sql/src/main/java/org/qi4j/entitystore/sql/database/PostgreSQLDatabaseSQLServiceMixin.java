/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.sql.common.SQLUtil;

/**
 *
 * @author Stanislav Muhametsin
 */
public class PostgreSQLDatabaseSQLServiceMixin extends AbstractDatabaseSQLService
{

    public static final String DEFAULT_SCHEMA_NAME = "qi4j_es";

    public static final String TABLE_NAME = "qi4j_entities";

    public static final String ENTITY_PK_COLUMN_NAME = "entity_pk";

    public static final String ENTITY_IDENTITY_COLUMN_NAME = "entity_id";

    public static final String ENTITY_STATE_COLUMN_NAME = "entity_state";

    public static final String ENTITY_PK_COLUMN_DATA_TYPE = "BIGINT";

    public static final String ENTITY_IDENTITY_COLUMN_DATA_TYPE = "TEXT";

    public static final String ENTITY_STATE_COLUMN_DATA_TYPE = "TEXT";

    public static final String READ_NEXT_ENTITY_PK_SQL = "SELECT COUNT(" + ENTITY_PK_COLUMN_NAME + "), MAX(" + ENTITY_PK_COLUMN_NAME + ") FROM %s." + TABLE_NAME;

    public static final String INSERT_ENTITY_SQL = "INSERT INTO %s." + TABLE_NAME + " VALUES(?, ?, ?)";

    public static final String REMOVE_ENTITY_SQL = "DELETE FROM %s." + TABLE_NAME + " WHERE " + ENTITY_PK_COLUMN_NAME + " = ?";

    public static final String UPDATE_ENTITY_SQL = "UPDATE %s." + TABLE_NAME + " SET " + ENTITY_STATE_COLUMN_NAME + " = ? WHERE " + ENTITY_PK_COLUMN_NAME + " = ?";

    public static final String SELECT_ALL_ENTITIES_SQL = "SELECT " + ENTITY_PK_COLUMN_NAME + ", " + ENTITY_STATE_COLUMN_NAME + " FROM %s." + TABLE_NAME;

    public static final String SELECT_ENTITY_SQL = SELECT_ALL_ENTITIES_SQL + " WHERE " + ENTITY_IDENTITY_COLUMN_NAME + " = ?";

    public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS %s." + TABLE_NAME + " CASCADE";

    public static final String CREATE_SCHEMA_SQL = "CREATE SCHEMA %s";

    public static final String CREATE_TABLE_SQL = "CREATE TABLE %s." + TABLE_NAME + "(" + "\n" + //
                                                  ENTITY_PK_COLUMN_NAME + " " + ENTITY_PK_COLUMN_DATA_TYPE + " NOT NULL PRIMARY KEY," + "\n" + //
                                                  ENTITY_IDENTITY_COLUMN_NAME + " " + ENTITY_IDENTITY_COLUMN_DATA_TYPE + " NOT NULL UNIQUE," + "\n" + //
                                                  ENTITY_STATE_COLUMN_NAME + " " + ENTITY_STATE_COLUMN_DATA_TYPE + " NOT NULL)";

    @This private Configuration<PostgreSQLConfiguration> _configuration;

    @Override
    protected Connection createConnection() throws SQLException
    {
        return DriverManager.getConnection( this._configuration.configuration().connectionString().get() );
    }

    @Override
    protected String getSchemaName( Connection connection )
        throws SQLException
    {
        String result = this._configuration.configuration().schemaName().get();
        if (result == null)
        {
            result = DEFAULT_SCHEMA_NAME;
        }

        return result;
    }

    @Override
    protected String getSQLForInsertEntityStatement()
    {
        return String.format( INSERT_ENTITY_SQL, this.getSchemaName() );
    }

    @Override
    protected String getSQLForRemoveEntityStatement()
    {
        return String.format( REMOVE_ENTITY_SQL, this.getSchemaName() );
    }

    @Override
    protected String getSQLForSelectAllEntitiesStatement()
    {
        return String.format( SELECT_ALL_ENTITIES_SQL, this.getSchemaName() );
    }

    @Override
    protected String getSQLForSelectEntityStatement()
    {
        return String.format( SELECT_ENTITY_SQL, this.getSchemaName() );
    }

    @Override
    protected String getSQLForUpdateEntityStatement()
    {
        return String.format( UPDATE_ENTITY_SQL, this.getSchemaName() );
    }

    public EntityValueResult getEntityValue( ResultSet rs )
        throws SQLException
    {
        return new EntityValueResult( rs.getCharacterStream( 2 ), rs.getLong( 1 ) );
    }

    public void populateGetAllEntitiesStatement( PreparedStatement ps )
        throws SQLException
    {
        // Nothing to do.
    }

    public void populateGetEntityStatement( PreparedStatement ps, EntityReference ref )
        throws SQLException
    {
        ps.setString( 1, ref.identity() );
    }

    public void populateInsertEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref, String entity )
        throws SQLException
    {
        ps.setLong( 1, entityPK );
        ps.setString( 2, ref.identity() );
        ps.setString( 3, entity );
    }

    public void populateRemoveEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref )
        throws SQLException
    {
        ps.setLong( 1, entityPK );
    }

    public void populateUpdateEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref, String entity )
        throws SQLException
    {
        ps.setString(1, entity);
        ps.setLong( 2, entityPK );
    }

    @Override
    protected boolean schemaExists( Connection connection )
        throws SQLException
    {
        ResultSet rs = null;

        try
        {
            Boolean schemaFound = false;
            rs = connection.getMetaData().getSchemas();
            String schemaName = this.getSchemaName();

            while (rs.next() && !schemaFound)
            {
                schemaFound = rs.getString(1).equals(schemaName);
            }
            return schemaFound;
        }
        finally
        {
           SQLUtil.closeQuietly( rs );
        }

    }

    @Override
    protected boolean tableExists( Connection connection )
        throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = connection.getMetaData().getTables(null, this.getSchemaName(), TABLE_NAME, new String[] { "TABLE" });
            return rs.next();
        } finally
        {
            SQLUtil.closeQuietly( rs );
        }
    }

    @Override
    protected String[] getSQLForIndexCreation()
    {
        // TODO
        return new String[] { };
    }

    @Override
    protected String[] getSQLForSchemaCreation()
    {
        return new String[] { String.format( CREATE_SCHEMA_SQL, this.getSchemaName() ) };
    }

    @Override
    protected String[] getSQLForTableCreation()
    {
        return new String[] { String.format( CREATE_TABLE_SQL, this.getSchemaName() ) };
    }

    @Override
    protected long readNextEntityPK( Connection connection )
        throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        long result = 0L;
        try
        {
            stmt = connection.createStatement();
            rs = stmt.executeQuery( String.format( READ_NEXT_ENTITY_PK_SQL, this.getSchemaName() ) );
            if (rs.next())
            {
                Long count = rs.getLong( 1 );
                if (count > 0)
                {
                    result = rs.getLong( 2 ) + 1;
                }
            }
        } finally
        {
            SQLUtil.closeQuietly( rs );
            SQLUtil.closeQuietly( stmt );
        }

        return result;
    }



}
