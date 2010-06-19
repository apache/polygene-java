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
package org.qi4j.entitystore.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.entitystore.sql.database.DatabaseService;
import org.qi4j.entitystore.sql.util.SQLUtil;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;

public class SQLEntityStoreMixin
        implements Activatable,
                   MapEntityStore
{

    @Service
    private DatabaseService dbService;

    public void activate()
            throws Exception
    {
        System.out.println( "SQLMapEntityStoreMixin: activate" );
        dbService.startDatabase();
    }

    public void passivate()
            throws Exception
    {
        System.out.println( "SQLMapEntityStoreMixin: passivate" );
        dbService.stopDatabase();
    }

    public Reader get( EntityReference er )
            throws EntityStoreException
    {
        Connection connection = null;
        PreparedStatement select = null;
        ResultSet resultSet = null;
        try {
            connection = dbService.openConnection();
            connection.setReadOnly( true );
            select = dbService.prepareGetEntityStatement( connection, er.identity() );
            resultSet = select.executeQuery();
            if ( !resultSet.next() ) {
                throw new EntityNotFoundException( er );
            }
            return dbService.getEntityValue( resultSet );
        } catch ( SQLException ex ) {
            throw new EntityStoreException( "Unable to get Entity " + er.identity(), ex );
        } finally {
            SQLUtil.closeQuietly( resultSet );
            SQLUtil.closeQuietly( select );
            SQLUtil.closeQuietly( connection );
        }
    }

    public void visitMap( MapEntityStoreVisitor mesv )
    {
        Connection connection = null;
        PreparedStatement select = null;
        ResultSet resultSet = null;
        try {
            connection = dbService.openConnection();
            connection.setReadOnly( true );
            select = dbService.prepareGetAllEntitiesStatement( connection );
            resultSet = select.executeQuery();
            while ( resultSet.next() ) {
                mesv.visitEntity( dbService.getEntityValue( resultSet ) );
            }
        } catch ( SQLException ex ) {
            throw new EntityStoreException( "Unable to visit MapEntityStore", ex );
        } finally {
            SQLUtil.closeQuietly( resultSet );
            SQLUtil.closeQuietly( select );
            SQLUtil.closeQuietly( connection );
        }
    }

    public void applyChanges( MapChanges mc )
            throws IOException
    {
        Connection connection = null;
        SQLMapChanger mapChanger = null;
        try {
            connection = dbService.openConnection();
            mapChanger = new SQLMapChanger( connection );
            mc.visitMap( mapChanger );
            connection.commit();
        } catch ( SQLException ex ) {
            SQLUtil.rollbackQuietly( connection );
            throw new EntityStoreException( "Unable to Apply Changes", ex );
        } finally {
            SQLUtil.closeQuietly( connection );
        }
    }

    private class SQLMapChanger
            implements MapChanger
    {

        private final Connection connection;

        private SQLMapChanger( Connection connection )
                throws SQLException
        {
            this.connection = connection;
        }

        public Writer newEntity( final EntityReference er, EntityType et )
                throws IOException
        {
            return new StringWriter( 1000 )
            {

                @Override
                public void close()
                        throws IOException
                {
                    super.close();
                    PreparedStatement insertNewEntity = null;
                    try {
                        insertNewEntity = dbService.prepareInsertEntityStatement( connection, er.identity(), toString() );
                        insertNewEntity.executeUpdate();
                    } catch ( SQLException ex ) {
                        throw new EntityStoreException( "Unable to insert new Entity: " + er.identity(), ex );
                    } finally {
                        SQLUtil.closeQuietly( insertNewEntity );
                    }
                }

            };
        }

        public Writer updateEntity( final EntityReference er, EntityType et )
                throws IOException
        {
            return new StringWriter( 1000 )
            {

                @Override
                public void close()
                        throws IOException
                {
                    super.close();
                    PreparedStatement updateEntity = null;
                    try {
                        updateEntity = dbService.prepareUpdateEntityStatement( connection, er.identity(), toString() );
                        updateEntity.executeUpdate();
                    } catch ( SQLException ex ) {
                        throw new EntityStoreException( "Unable to update Entity: " + er.identity(), ex );
                    } finally {
                        SQLUtil.closeQuietly( updateEntity );
                    }
                }

            };
        }

        public void removeEntity( EntityReference er, EntityType et )
                throws EntityNotFoundException
        {
            PreparedStatement removeEntity = null;
            try {
                removeEntity = dbService.prepareRemoveEntityStatement( connection, er.identity() );
                removeEntity.executeUpdate();
            } catch ( SQLException ex ) {
                throw new EntityStoreException( "Unable to remove Entity: " + er.identity(), ex );
            } finally {
                SQLUtil.closeQuietly( removeEntity );
            }
        }

    }

}
