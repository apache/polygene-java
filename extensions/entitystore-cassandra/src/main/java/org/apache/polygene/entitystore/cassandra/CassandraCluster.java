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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceActivation;

@Mixins( CassandraCluster.Mixin.class )
public interface CassandraCluster
{
    String CURRENT_STORAGE_VERSION = "1";
    String DEFAULT_KEYSPACE_NAME = "polygene:entitystore";
    String DEFAULT_TABLE_NAME = "polygene:entitystore:entities";
    String IDENTITY_COLUMN = "_id";
    String VERSION_COLUMN = "_version";
    String USECASE_COLUMN = "_usecase";
    String LASTMODIFIED_COLUMN = "_modified";
    String APP_VERSION_COLUMN = "_appversion";
    String STORE_VERSION_COLUMN = "_storeversion";
    String TYPE_COLUMN = "_type";
    String PROPERTIES_COLUMN = "_props";
    String ASSOCIATIONS_COLUMN = "_assocs";
    String MANYASSOCIATIONS_COLUMN = "_manyassocs";
    String NAMEDASSOCIATIONS_COLUMN = "_namedassocs";

    Session session();

    String tableName();

    PreparedStatement entityRetrieveStatement();
    PreparedStatement versionRetrieveStatement();
    PreparedStatement entityUpdateStatement();

    String keyspaceName();

    class Mixin
        implements ServiceActivation, CassandraCluster
    {
        @This
        private Configuration<CassandraEntityStoreConfiguration> configuration;

        private Cluster cluster;
        private Session session;
        private String keyspaceName;
        private PreparedStatement getEntityStatement;
        private PreparedStatement updateEntityStatement;
        private PreparedStatement getVersionStatement;

        @Override
        public Session session()
        {
            return session;
        }

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

        public  String tableName(  )
        {
            CassandraEntityStoreConfiguration config = configuration.get();
            String tableName = config.table().get();
            if( tableName == null )
            {
                tableName = DEFAULT_TABLE_NAME;
            }
            return tableName;
        }

        @Override
        public void activateService()
            throws Exception
        {
            configuration.refresh();
            CassandraEntityStoreConfiguration config = configuration.get();

            String[] hostNames = config.hostnames().get().split( "," );
            Cluster.Builder builder =
                Cluster.builder()
                       .withClusterName( "myCluster" )
                       .addContactPoints( hostNames )
                       .withCredentials( config.username().get(), config.password().get() );
            cluster = builder.build();
            keyspaceName = config.keySpace().get();
            if( keyspaceName == null )
            {
                keyspaceName = DEFAULT_KEYSPACE_NAME;
            }
            String tableName = tableName();
            KeyspaceMetadata keyspace = cluster.getMetadata().getKeyspace( keyspaceName );
            if( keyspace == null )
            {
                createKeyspace( keyspaceName, config.replicationFactor().get() );
                session = cluster.connect( keyspaceName );
                createPolygeneStateTable( tableName );
            }
            else
            {
                session = cluster.connect( keyspaceName );
            }
            session.init();

            getEntityStatement = session.prepare( "SELECT "
                                                  + IDENTITY_COLUMN + ", "
                                                  + VERSION_COLUMN + ", "
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
                                                   + VERSION_COLUMN + ", "
                                                   + " FROM " + tableName
                                                   + " WHERE "
                                                   + IDENTITY_COLUMN + " = ?" );

            updateEntityStatement = session.prepare( "INSERT INTO " + tableName + "( "
                                                     + IDENTITY_COLUMN + ", "
                                                     + VERSION_COLUMN + ", "
                                                     + APP_VERSION_COLUMN + ", "
                                                     + STORE_VERSION_COLUMN + ", "
                                                     + LASTMODIFIED_COLUMN + ", "
                                                     + USECASE_COLUMN + ", "
                                                     + PROPERTIES_COLUMN + ", "
                                                     + ASSOCIATIONS_COLUMN + ", "
                                                     + MANYASSOCIATIONS_COLUMN + ", "
                                                     + NAMEDASSOCIATIONS_COLUMN
                                                     + " ) VALUES (?,?,?," + CURRENT_STORAGE_VERSION + "?,?,?,?,?,?)" );
        }

        private void createPolygeneStateTable( String tableName )
        {
            session.execute( "CREATE TABLE " + tableName + "(\n"
                             + "   " + IDENTITY_COLUMN + " text PRIMARYKEY,\n"
                             + "   " + VERSION_COLUMN + " text,\n"
                             + "   " + APP_VERSION_COLUMN + " text,\n"
                             + "   " + STORE_VERSION_COLUMN + " text,\n"
                             + "   " + LASTMODIFIED_COLUMN + " timestamp,\n"
                             + "   " + USECASE_COLUMN + " text,\n"
                             + "   " + PROPERTIES_COLUMN + " map,\n"
                             + "   " + ASSOCIATIONS_COLUMN + " map,\n"
                             + "   " + MANYASSOCIATIONS_COLUMN + " map,\n"
                             + "   " + NAMEDASSOCIATIONS_COLUMN + " map,\n"
                             + "   )" );
        }

        private void createKeyspace( String keyspaceName, int replication )
        {
            try( Session defaultSession = cluster.connect() )
            {
                String query = "CREATE KEYSPACE " + keyspaceName + "WITH replication = {'class':'SimpleStrategy', 'replication_factor' : " + replication + "};";
                defaultSession.execute( query );
            }
        }

        @Override
        public void passivateService()
            throws Exception
        {
            cluster.close();
        }
    }
}