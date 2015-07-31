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

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Riak implementation of MapEntityStore.
 */
/* package */ abstract class AbstractRiakMapEntityStore
    implements ServiceActivation, MapEntityStore, RiakAccessors
{

    protected static final Logger LOGGER = LoggerFactory.getLogger( "org.qi4j.entitystore.riak" );
    protected static final int DEFAULT_MAX_CONNECTIONS = 50;

    /* package */ static final String DEFAULT_BUCKET_KEY = "qi4j:entities";
    protected IRiakClient riakClient;
    protected String bucketKey;

    @Override
    public void passivateService()
        throws Exception
    {
        riakClient.shutdown();
        riakClient = null;
        bucketKey = null;
    }

    @Override
    public IRiakClient riakClient()
    {
        return riakClient;
    }

    @Override
    public String bucket()
    {
        return bucketKey;
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try
        {

            Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
            IRiakObject entity = bucket.fetch( entityReference.identity() ).execute();
            if( entity == null )
            {
                throw new EntityNotFoundException( entityReference );
            }
            String jsonState = entity.getValueAsString();
            return new StringReader( jsonState );

        }
        catch( RiakRetryFailedException ex )
        {
            throw new EntityStoreException( "Unable to get Entity " + entityReference.identity(), ex );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        try
        {
            final Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();

            changes.visitMap( new MapChanger()
            {

                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            try
                            {
                                super.close();
                                bucket.store( ref.identity(), toString() ).execute();
                            }
                            catch( RiakException ex )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: newEntity", ex );
                            }
                        }

                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                            throws IOException
                        {
                            try
                            {
                                super.close();
                                IRiakObject entity = bucket.fetch( ref.identity() ).execute();
                                if( entity == null )
                                {
                                    throw new EntityNotFoundException( ref );
                                }
                                bucket.store( ref.identity(), toString() ).execute();
                            }
                            catch( RiakException ex )
                            {
                                throw new EntityStoreException( "Unable to apply entity change: updateEntity", ex );
                            }
                        }

                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                    throws EntityNotFoundException
                {
                    try
                    {
                        IRiakObject entity = bucket.fetch( ref.identity() ).execute();
                        if( entity == null )
                        {
                            throw new EntityNotFoundException( ref );
                        }
                        bucket.delete( ref.identity() ).execute();
                    }
                    catch( RiakException ex )
                    {
                        throw new EntityStoreException( "Unable to apply entity change: removeEntity", ex );
                    }
                }

            } );

        }
        catch( RiakRetryFailedException ex )
        {
            throw new EntityStoreException( "Unable to apply entity changes.", ex );
        }
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        try
                        {
                            final Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
                            for( String key : bucket.keys() )
                            {
                                String jsonState = bucket.fetch( key ).execute().getValueAsString();
                                receiver.receive( new StringReader( jsonState ) );
                            }
                        }
                        catch( RiakException ex )
                        {
                            throw new EntityStoreException( "Unable to apply entity changes.", ex );
                        }
                    }

                } );
            }

        };
    }

}
