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
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.polygene.api.mixin.Mixins;

@Mixins( ClusterBuilder.DefaultBuilder.class )
public interface ClusterBuilder
{
    String DEFAULT_HOST_PORT = "localhost:9042";

    Cluster build(CassandraEntityStoreConfiguration config);

    class DefaultBuilder
        implements ClusterBuilder
    {

        protected CassandraEntityStoreConfiguration config;

        @Override
        public Cluster build( CassandraEntityStoreConfiguration config )
        {
            this.config = config;
            String clusterName = clusterName( config );
            Collection<InetSocketAddress> connectionPoints = cassandraConnectionPoints();
            Cluster.Builder builder =
                Cluster.builder()
                       .withClusterName( clusterName )
                       .addContactPointsWithPorts(connectionPoints)
                       .withCredentials( username(), password() );
            builder = customConfiguration(builder);
            return builder.build();
        }

        protected String clusterName( CassandraEntityStoreConfiguration config )
        {
            String clusterName = config.clusterName().get();
            if( clusterName == null )
            {
                clusterName = "polygene-cluster";
            }
            return clusterName;
        }

        protected String username()
        {
            return config.username().get();
        }

        protected String password()
        {
            return config.password().get();
        }

        protected Collection<InetSocketAddress> cassandraConnectionPoints()
        {
            String hostnames = hostnames();
            return Arrays.stream( hostnames.split( "(,| )" ) )
                         .map( text ->
                        {
                            String[] strings = text.split( ":" );
                            return new InetSocketAddress( strings[ 0 ], Integer.parseInt( strings[ 1 ] ) );
                        }
                      )
                         .collect( Collectors.toList() );
        }

        protected String hostnames()
        {
            String hostnames = config.hostnames().get();
            if( hostnames == null )
            {
                hostnames = DEFAULT_HOST_PORT;
            }
            return hostnames;
        }

        protected Cluster.Builder customConfiguration( Cluster.Builder builder )
        {
            return builder;
        }
    }
}
