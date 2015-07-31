/*
 * Copyright 2012 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.riak;

import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.raw.http.HTTPClientConfig;
import com.basho.riak.client.raw.http.HTTPClusterConfig;
import java.util.List;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;

/**
 * Riak Http implementation of MapEntityStore.
 */
public class RiakHttpMapEntityStoreMixin
    extends AbstractRiakMapEntityStore
{

    private static final String DEFAULT_URL = "http://127.0.0.1:8098/riak";
    @This
    private Configuration<RiakHttpEntityStoreConfiguration> configuration;

    @Override
    public void activateService()
        throws Exception
    {
        configuration.refresh();
        RiakHttpEntityStoreConfiguration config = configuration.get();

        int maxConnections = config.maxConnections().get() == null ? DEFAULT_MAX_CONNECTIONS : config.maxConnections().get();
        int timeoutMillis = config.timeout().get();
        List<String> urls = config.urls().get();
        if( urls.isEmpty() )
        {
            urls.add( DEFAULT_URL );
        }
        bucketKey = config.bucket().get() == null ? DEFAULT_BUCKET_KEY : config.bucket().get();

        HTTPClusterConfig httpClusterConfig = new HTTPClusterConfig( maxConnections );
        for( String url : urls )
        {
            HTTPClientConfig clientConfig = new HTTPClientConfig.Builder().withTimeout( timeoutMillis ).withUrl( url ).build();
            httpClusterConfig.addClient( clientConfig );
        }
        riakClient = RiakFactory.newClient( httpClusterConfig );

        if( !riakClient.listBuckets().contains( bucketKey ) )
        {
            riakClient.createBucket( bucketKey ).execute();
        }
    }

}
