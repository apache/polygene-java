/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.sql.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.qi4j.functional.Visitor;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;

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
        try {
            connection = source.getConnection();
            stmt = connection.prepareStatement( sql );
            return stmt.executeUpdate();
        } finally {
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
        try {
            connection = source.getConnection();
            stmt = connection.prepareStatement( sql );
            visitor.visit( stmt );
            return stmt.executeUpdate();
        } finally {
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
    public void query( String sql, StatementVisitor statement, ResultSetVisitor resultsetVisitor )
            throws SQLException
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            connection = source.getConnection();
            stmt = connection.prepareStatement( sql );
            if ( statement != null ) {
                statement.visit( stmt );
            }
            resultSet = stmt.executeQuery();
            while ( resultSet.next() ) {
                if ( !resultsetVisitor.visit( resultSet ) ) {
                    return;
                }
            }
            resultSet.close();
        } finally {
            SQLUtil.closeQuietly( resultSet );
            SQLUtil.closeQuietly( stmt );
            SQLUtil.closeQuietly( connection );
        }
    }

    /**
     * Perform SQL query and provide results as an Input.
     *
     * This makes it possible to combine SQL data with the I/O API.
     */
    public Input<ResultSet, SQLException> query( final String sql )
    {
        return new Input<ResultSet, SQLException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super ResultSet, ReceiverThrowableType> output )
                    throws SQLException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<ResultSet, SQLException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super ResultSet, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, SQLException
                    {
                        query( sql, new ResultSetVisitor()
                        {

                            @Override
                            public boolean visit( ResultSet visited )
                                    throws SQLException
                            {
                                try {
                                    receiver.receive( visited );
                                } catch ( Throwable receiverThrowableType ) {
                                    throw new SQLException( receiverThrowableType );
                                }

                                return true;
                            }

                        } );
                    }

                } );
            }

        };
    }

    /**
     * Perform SQL query and provide results as an Input.
     *
     * This makes it possible to combine SQL data with the I/O API. If the SQL
     * string contains ? placeholders, use the StatementVisitor to update the
     * PreparedStatement with actual values.
     */
    public Input<ResultSet, SQLException> query( final String sql, final StatementVisitor visitor )
    {
        return new Input<ResultSet, SQLException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super ResultSet, ReceiverThrowableType> output )
                    throws SQLException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<ResultSet, SQLException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<? super ResultSet, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, SQLException
                    {
                        query( sql, visitor, new ResultSetVisitor()
                        {

                            @Override
                            public boolean visit( ResultSet visited )
                                    throws SQLException
                            {
                                try {
                                    receiver.receive( visited );
                                } catch ( Throwable receiverThrowableType ) {
                                    throw new SQLException( receiverThrowableType );
                                }

                                return true;
                            }

                        } );
                    }

                } );
            }

        };
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
