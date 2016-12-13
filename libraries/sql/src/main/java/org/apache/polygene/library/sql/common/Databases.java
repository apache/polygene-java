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
package org.apache.polygene.library.sql.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.polygene.api.util.Visitor;

/**
 * Utility methods for performing SQL calls wrapping a given DataSource.
 */
public class Databases
{
    DataSource source;

    /**
     * Create a new Databases wrapper for a given DataSource.
     */
    public Databases( DataSource source )
    {
        this.source = source;
    }

    /**
     * Perform SQL update statement.
     */
    public int update( String sql )
        throws SQLException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = source.getConnection();
            stmt = connection.prepareStatement( sql );
            return stmt.executeUpdate();
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }
    }

    /**
     * Perform SQL update statement.
     *
     * If the SQL string contains ? placeholders, use the StatementVisitor to
     * update the PreparedStatement with actual values.
     */
    public int update( String sql, StatementVisitor visitor )
        throws SQLException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = source.getConnection();
            stmt = connection.prepareStatement( sql );
            visitor.visit( stmt );
            return stmt.executeUpdate();
        }
        finally
        {
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }
    }

    /**
     * Perform SQL query and let visitor handle results.
     */
    public void query( String sql, ResultSetVisitor visitor )
        throws SQLException
    {
        query( sql, null, visitor );
    }

    /**
     * Perform SQL query and let visitor handle results.
     *
     * If the SQL string contains ? placeholders, use the StatementVisitor to
     * update the PreparedStatement with actual values.
     */
    public void query( String sql, StatementVisitor statementVisitor, ResultSetVisitor resultSetVisitor )
        throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            connection = source.getConnection();
            statement = connection.prepareStatement( sql );
            if( statementVisitor != null )
            {
                statementVisitor.visit( statement );
            }
            resultSet = statement.executeQuery();
            while( resultSet.next() )
            {
                if( !resultSetVisitor.visit( resultSet ) )
                {
                    return;
                }
            }
            resultSet.close();
        }
        finally
        {
            SQLUtil.closeQuietly( resultSet );
            SQLUtil.closeQuietly( statement );
            SQLUtil.closeQuietly( connection );
        }
    }

    /**
     * Visitor for PreparedStatements.
     *
     * These are created when the SQL statements contain ? placeholders.
     */
    public interface StatementVisitor
    {
        void visit( PreparedStatement preparedStatement )
            throws SQLException;
    }

    /**
     * Visitor for the ResultSet.
     *
     * Only access data from the given ResultSet, as the iteration will be done
     * by this API.
     */
    public interface ResultSetVisitor
        extends Visitor<ResultSet, SQLException>
    {
    }
}
