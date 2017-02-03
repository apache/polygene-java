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

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.service.ServiceComposite;

@SuppressWarnings( "PublicInnerClass" )
public interface DatabaseSQLService
{
    interface DatabaseSQLServiceComposite extends DatabaseSQLService, ServiceComposite
    {
    }

    final class EntityValueResult
    {
        private final Reader reader;

        EntityValueResult( Reader reader )
        {
            this.reader = reader;
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

    void populateInsertEntityStatement( PreparedStatement ps, EntityReference ref, String entity )
        throws SQLException;

    void populateUpdateEntityStatement( PreparedStatement ps, EntityReference ref, String entity )
        throws SQLException;

    void populateRemoveEntityStatement( PreparedStatement ps, EntityReference ref )
        throws SQLException;

    Reader getEntityStateReader( ResultSet rs )
        throws SQLException;
}
