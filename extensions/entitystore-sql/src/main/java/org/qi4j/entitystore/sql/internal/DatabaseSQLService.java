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

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.service.ServiceComposite;

@SuppressWarnings( "PublicInnerClass" )
public interface DatabaseSQLService
{

    public interface DatabaseSQLServiceComposite
        extends DatabaseSQLService, ServiceComposite
    {
    }

    public final class EntityValueResult
    {

        private final Long entityPK;

        private final Long entityOptimisticLock;

        private final Reader reader;

        public EntityValueResult( Long entityPK, Long entityOptimisticLock, Reader reader )
        {
            this.entityPK = entityPK;
            this.entityOptimisticLock = entityOptimisticLock;
            this.reader = reader;
        }

        /**
         * @return the entityPK
         */
        public Long getEntityPK()
        {
            return entityPK;
        }

        /**
         * @return the entityOptimisticLock
         */
        public Long getEntityOptimisticLock()
        {
            return entityOptimisticLock;
        }

        /**
         * @return the reader
         */
        public Reader getReader()
        {
            return reader;
        }

    }

    void startDatabase()
        throws Exception;

    void stopDatabase()
        throws Exception;

    Connection getConnection()
        throws SQLException;

    PreparedStatement prepareGetEntityStatement( Connection connection )
        throws SQLException;

    PreparedStatement prepareGetAllEntitiesStatement( Connection connection )
        throws SQLException;

    PreparedStatement prepareInsertEntityStatement( Connection connection )
        throws SQLException;

    PreparedStatement prepareUpdateEntityStatement( Connection connection )
        throws SQLException;

    PreparedStatement prepareRemoveEntityStatement( Connection connection )
        throws SQLException;

    void populateGetEntityStatement( PreparedStatement ps, EntityReference ref )
        throws SQLException;

    void populateGetAllEntitiesStatement( PreparedStatement ps )
        throws SQLException;

    void populateInsertEntityStatement( PreparedStatement ps, EntityReference ref, String entity, Long lastModified )
        throws SQLException;

    void populateUpdateEntityStatement( PreparedStatement ps, Long entityPK, Long entityOptimisticLock, EntityReference ref, String entity, Long lastModified )
        throws SQLException;

    void populateRemoveEntityStatement( PreparedStatement ps, Long entityPK, EntityReference ref )
        throws SQLException;

    EntityValueResult getEntityValue( ResultSet rs )
        throws SQLException;

}
