/*
 * Copyright 2011 Paul Merlin.
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
package org.qi4j.entitystore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
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
 * MongoDB implementation of MapEntityStore.
 */
public class MongoMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore, MongoAccessors
{

    private static final Logger LOGGER = LoggerFactory.getLogger( "org.qi4j.entitystore.mongodb" );
    private static final String DEFAULT_DATABASE_NAME = "qi4j:entitystore";
    private static final String DEFAULT_COLLECTION_NAME = "qi4j:entitystore:entities";
    public static final String IDENTITY_COLUMN = "identity";
    public static final String STATE_COLUMN = "state";
    @This
    private Configuration<MongoEntityStoreConfiguration> configuration;
    private List<ServerAddress> serverAddresses;
    private String databaseName;
    private String collectionName;
    private WriteConcern writeConcern;
    private String username;
    private char[] password;
    private Mongo mongo;
    private DB db;

    @Override
    public void activateService()
        throws Exception
    {
        loadConfiguration();

        // Create Mongo driver and open the database
        mongo = new Mongo( serverAddresses );
        db = mongo.getDB( databaseName );

        // Authenticate if needed
        if( !username.isEmpty() )
        {
            if( !db.authenticate( username, password ) )
            {
                LOGGER.warn( "Authentication against MongoDB with username '" + username + "' failed. Subsequent requests will be made 'anonymously'." );
            }
        }

        // Create index if needed
        db.requestStart();
        DBCollection entities = db.getCollection( collectionName );
        if( entities.getIndexInfo().isEmpty() )
        {
            entities.createIndex( new BasicDBObject( IDENTITY_COLUMN, 1 ) );
        }
        db.requestDone();
    }

    private void loadConfiguration()
        throws UnknownHostException
    {
        configuration.refresh();
        MongoEntityStoreConfiguration config = configuration.get();

        // Combine hostname, port and nodes configuration properties
        serverAddresses = new ArrayList<ServerAddress>();
        if( config.hostname().get() != null && !config.hostname().get().isEmpty() )
        {
            serverAddresses.add( new ServerAddress( config.hostname().get(), config.port().get() ) );
        }
        serverAddresses.addAll( config.nodes().get() );

        // If database name not configured, set it to qi4j:entitystore
        databaseName = config.database().get();
        if( databaseName == null )
        {
            databaseName = DEFAULT_DATABASE_NAME;
        }

        // If collection name not configured, set it to qi4j:entitystore:entities
        collectionName = config.collection().get();
        if( collectionName == null )
        {
            collectionName = DEFAULT_COLLECTION_NAME;
        }

        // If write concern not configured, set it to normal
        switch( config.writeConcern().get() )
        {
            case FSYNC_SAFE:
                writeConcern = WriteConcern.FSYNC_SAFE;
                break;
            case JOURNAL_SAFE:
                writeConcern = WriteConcern.JOURNAL_SAFE;
                break;
            case MAJORITY:
                writeConcern = WriteConcern.MAJORITY;
                break;
            case NONE:
                writeConcern = WriteConcern.NONE;
                break;
            case REPLICAS_SAFE:
                writeConcern = WriteConcern.REPLICAS_SAFE;
                break;
            case SAFE:
                writeConcern = WriteConcern.SAFE;
                break;
            case NORMAL:
            default:
                writeConcern = WriteConcern.NORMAL;
        }

        // Username and password are defaulted to empty strings
        username = config.username().get();
        password = config.password().get().toCharArray();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        mongo.close();
        mongo = null;
        databaseName = null;
        collectionName = null;
        writeConcern = null;
        username = null;
        Arrays.fill( password, ' ' );
        password = null;
        db = null;
    }

    @Override
    public Mongo mongoInstanceUsed()
    {
        return mongo;
    }

    @Override
    public DB dbInstanceUsed()
    {
        return db;
    }

    @Override
    public String collectionUsed()
    {
        return collectionName;
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        db.requestStart();

        DBObject entity = db.getCollection( collectionName ).findOne( byIdentity( entityReference ) );
        if( entity == null )
        {
            throw new EntityNotFoundException( entityReference );
        }
        DBObject bsonState = (DBObject) entity.get( STATE_COLUMN );

        db.requestDone();

        String jsonState = JSON.serialize( bsonState );
        return new StringReader( jsonState );
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        db.requestStart();
        final DBCollection entities = db.getCollection( collectionName );

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
                        super.close();

                        String jsonState = toString();
                        System.out.println( "############################################" );
                        try
                        {
                            System.out.println( new JSONObject( jsonState ).toString( 2 ) );
                        }
                        catch( JSONException ex )
                        {
                            ex.printStackTrace();
                        }
                        System.out.println( "############################################" );
                        DBObject bsonState = (DBObject) JSON.parse( jsonState );

                        BasicDBObject entity = new BasicDBObject();
                        entity.put( IDENTITY_COLUMN, ref.identity() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.save( entity, writeConcern );
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
                        super.close();

                        DBObject bsonState = (DBObject) JSON.parse( toString() );

                        BasicDBObject entity = new BasicDBObject();
                        entity.put( IDENTITY_COLUMN, ref.identity() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.update( byIdentity( ref ), entity, true, false, writeConcern );
                    }
                };
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws EntityNotFoundException
            {
                DBObject entity = entities.findOne( byIdentity( ref ) );
                if( entity == null )
                {
                    throw new EntityNotFoundException( ref );
                }
                entities.remove( entity, writeConcern );
            }
        } );

        db.requestDone();
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
                        db.requestStart();

                        DBCursor cursor = db.getCollection( collectionName ).find();
                        while( cursor.hasNext() )
                        {
                            DBObject eachEntity = cursor.next();
                            DBObject bsonState = (DBObject) eachEntity.get( STATE_COLUMN );
                            String jsonState = JSON.serialize( bsonState );
                            receiver.receive( new StringReader( jsonState ) );
                        }

                        db.requestDone();
                    }
                } );
            }
        };
    }

    private DBObject byIdentity( EntityReference entityReference )
    {
        return new BasicDBObject( IDENTITY_COLUMN, entityReference.identity() );
    }
}
