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
package org.apache.polygene.entitystore.cassandra;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.spi.entitystore.EntityStoreException;

@Mixins( CassandraCluster.Mixin.class )
public interface CassandraCluster
{
    String IDENTITY_COLUMN = "id";
    String STORE_VERSION_COLUMN = "storeversion";
    String VERSION_COLUMN = "version";
    String APP_VERSION_COLUMN = "appversion";
    String USECASE_COLUMN = "usecase";
    String LASTMODIFIED_COLUMN = "modified";
    String TYPE_COLUMN = "type";
    String PROPERTIES_COLUMN = "props";
    String ASSOCIATIONS_COLUMN = "assocs";
    String MANYASSOCIATIONS_COLUMN = "manyassocs";
    String NAMEDASSOCIATIONS_COLUMN = "namedassocs";

    String tableName();

    Session cassandraClientSession();

    PreparedStatement entityRetrieveStatement();

    PreparedStatement versionRetrieveStatement();

    PreparedStatement entityUpdateStatement();

    String keyspaceName();

    void activate()
        throws Exception;

    void passivate()
        throws Exception;

    class Mixin
        implements CassandraCluster
    {
        @This
        private Configuration<CassandraEntityStoreConfiguration> configuration;

        @Service
        @Optional
        private AuthProvider authProvider;

        @This
        private ClusterBuilder clusterBuilder;

        private Cluster cluster;
        private Session session;
        private String keyspaceName;
        private PreparedStatement getEntityStatement;
        private PreparedStatement updateEntityStatement;
        private PreparedStatement getVersionStatement;

        @Override
        public PreparedStatement entityRetrieveStatement()
        {
            return getEntityStatement;
        }

        @Override
        public PreparedStatement versionRetrieveStatement()
        {
            return getVersionStatement;
        }

        @Override
        public PreparedStatement entityUpdateStatement()
        {
            return updateEntityStatement;
        }

        @Override
        public String keyspaceName()
        {
            return keyspaceName;
        }

        @Override
        public Session cassandraClientSession()
        {
            return session;
        }

        public String tableName()
        {
            CassandraEntityStoreConfiguration config = configuration.get();
            String tableName = config.entityTableName().get();
            if( tableName == null || tableName.isEmpty() )
            {
                tableName = CassandraEntityStoreService.DEFAULT_TABLE_NAME;
            }
            return tableName;
        }

        public void activate()
            throws Exception
        {
            configuration.refresh();
            CassandraEntityStoreConfiguration config = configuration.get();
            cluster = clusterBuilder.build( config );
            keyspaceName = config.keySpace().get();
            if( keyspaceName == null || keyspaceName.isEmpty() )
            {
                keyspaceName = CassandraEntityStoreService.DEFAULT_KEYSPACE_NAME;
            }
            String tableName = tableName();
            KeyspaceMetadata keyspace = cluster.getMetadata().getKeyspace( keyspaceName );
            boolean createIfMissing = config.createIfMissing().get();
            if( keyspace == null )
            {
                if( createIfMissing )
                {
                    Integer replication = config.replicationFactor().get();
                    if( replication == null || replication <= 0 )
                    {
                        replication = 3;
                    }
                    createKeyspace( keyspaceName, replication );
                    session = cluster.connect( keyspaceName );
                }
                else
                {
                    throw new EntityStoreException( "Keyspace '" + keyspaceName + "' does not exist." );
                }
            }
            else
            {
                session = cluster.connect( keyspaceName );
            }
            session.init();
            if( createIfMissing )
            {
                createPolygeneStateTable( tableName );
            }
            getEntityStatement = session.prepare( "SELECT "
                                                  + IDENTITY_COLUMN + ", "
                                                  + VERSION_COLUMN + ", "
                                                  + TYPE_COLUMN + ", "
                                                  + APP_VERSION_COLUMN + ", "
                                                  + STORE_VERSION_COLUMN + ", "
                                                  + LASTMODIFIED_COLUMN + ", "
                                                  + USECASE_COLUMN + ", "
                                                  + PROPERTIES_COLUMN + ", "
                                                  + ASSOCIATIONS_COLUMN + ", "
                                                  + MANYASSOCIATIONS_COLUMN + ", "
                                                  + NAMEDASSOCIATIONS_COLUMN
                                                  + " FROM " + tableName
                                                  + " WHERE "
                                                  + IDENTITY_COLUMN + " = ?" );

            getVersionStatement = session.prepare( "SELECT "
                                                   + VERSION_COLUMN
                                                   + " FROM " + tableName
                                                   + " WHERE "
                                                   + IDENTITY_COLUMN + " = ?" );

            updateEntityStatement = session.prepare( "INSERT INTO " + tableName + "( "
                                                     + IDENTITY_COLUMN + ", "               // id
                                                     + VERSION_COLUMN + ", "                // version
                                                     + TYPE_COLUMN + ", "                   // type
                                                     + APP_VERSION_COLUMN + ", "            // appversion
                                                     + STORE_VERSION_COLUMN + ", "          // storeversion
                                                     + LASTMODIFIED_COLUMN + ", "           // lastmodified
                                                     + USECASE_COLUMN + ", "                // usecase
                                                     + PROPERTIES_COLUMN + ", "             // properties
                                                     + ASSOCIATIONS_COLUMN + ", "           // associations
                                                     + MANYASSOCIATIONS_COLUMN + ", "       // manyassociations
                                                     + NAMEDASSOCIATIONS_COLUMN             // namedassociations
                                                     + " ) VALUES (?,?,?,?,?,?,?,?,?,?,?)" );
        }

        private void createPolygeneStateTable( String tableName )
        {
            try
            {
                session.execute( "CREATE TABLE " + tableName + "(\n"
                                 + "   " + IDENTITY_COLUMN + " text,\n"
                                 + "   " + VERSION_COLUMN + " text,\n"
                                 + "   " + TYPE_COLUMN + " text,\n"
                                 + "   " + APP_VERSION_COLUMN + " text,\n"
                                 + "   " + STORE_VERSION_COLUMN + " text,\n"
                                 + "   " + LASTMODIFIED_COLUMN + " timestamp,\n"
                                 + "   " + USECASE_COLUMN + " text,\n"
                                 + "   " + PROPERTIES_COLUMN + " map<text,text>,\n"
                                 + "   " + ASSOCIATIONS_COLUMN + " map<text,text>,\n"
                                 + "   " + MANYASSOCIATIONS_COLUMN + " map<text,text>,\n"
                                 + "   " + NAMEDASSOCIATIONS_COLUMN + " map<text,text>,\n"
                                 + "   PRIMARY KEY ( " + IDENTITY_COLUMN + " )\n"
                                 + "   )" );
            }
            catch( AlreadyExistsException e )
            {
                // This is OK, as we try to create on every connect().
            }
        }

        private void createKeyspace( String keyspaceName, int replication )
        {
            try( Session defaultSession = cluster.connect() )
            {
                String query = "CREATE KEYSPACE " + keyspaceName + " WITH replication = {'class':'SimpleStrategy', 'replication_factor' : " + replication + "};";
                defaultSession.execute( query );
            }
        }

        public void passivate()
            throws Exception
        {
            cluster.close();
        }
    }
}