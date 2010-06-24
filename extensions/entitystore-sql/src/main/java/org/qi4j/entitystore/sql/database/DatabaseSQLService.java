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

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

/**
 *
 * @author Stanislav Muhametsin
 */
public interface DatabaseSQLService
{
    public interface DatabaseSQLServiceComposite extends DatabaseSQLService, ServiceComposite
    {

    }

    public final class EntityValueResult
    {
        private final Reader _reader;

        private final Long _entityPK;

        public EntityValueResult( Reader reader, Long entityPK )
        {
            this._reader = reader;
            this._entityPK = entityPK;
        }

        /**
         * @return the entityPK
         */
        public Long getEntityPK()
        {
            return this._entityPK;
        }

        /**
         * @return the reader
         */
        public Reader getReader()
        {
            return this._reader;
        }
    }

    public void startDatabase()
        throws Exception;

    public void stopDatabase()
        throws Exception;

    public Connection getConnection()
        throws SQLException;

    public PreparedStatement prepareGetEntityStatement( Connection connection )
        throws SQLException;

    public PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
        throws SQLException;

    public PreparedStatement prepareInsertEntityStatement( Connection connection )
        throws SQLException;

    public PreparedStatement prepareUpdateEntityStatement( Connection connection )
        throws SQLException;

    public PreparedStatement prepareRemoveEntityStatement( Connection connection )
        throws SQLException;

    public void populateGetEntityStatement( PreparedStatement ps, EntityReference ref )
        throws SQLException;

    public void populateGetAllEntitiesStatement( PreparedStatement ps )
        throws SQLException;

    public void populateInsertEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref, String entity )
        throws SQLException;

    public void populateUpdateEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref, String entity )
        throws SQLException;

    public void populateRemoveEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref )
        throws SQLException;

    public EntityValueResult getEntityValue( ResultSet rs )
        throws SQLException;

    public Long newPKForEntity();
}
