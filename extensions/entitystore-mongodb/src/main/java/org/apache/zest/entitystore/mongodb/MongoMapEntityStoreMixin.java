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
package org.apache.zest.entitystore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
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
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;

/**
 * MongoDB implementation of MapEntityStore.
 */
public class MongoMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore, MongoAccessors
{
    private static final String DEFAULT_DATABASE_NAME = "qi4j:entitystore";
    private static final String DEFAULT_COLLECTION_NAME = "qi4j:entitystore:entities";
    public static final String IDENTITY_COLUMN = "_id";
    public static final String STATE_COLUMN = "state";
    @This
    private Configuration<MongoEntityStoreConfiguration> configuration;
    private List<ServerAddress> serverAddresses;
    private String databaseName;
    private String collectionName;
    private WriteConcern writeConcern;
    private String username;
    private char[] password;
    private MongoClient mongo;
    private DB db;

    @Override
    public void activateService()
        throws Exception
    {
        loadConfiguration();

        // Create Mongo driver and open the database
        if( username.isEmpty() )
        {
            mongo = new MongoClient( serverAddresses );
        }
        else
        {
            MongoCredential credential = MongoCredential.createMongoCRCredential( username, databaseName, password );
            mongo = new MongoClient( serverAddresses, Arrays.asList( credential ) );
        }
        db = mongo.getDB( databaseName );

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
        // If no configuration, use 127.0.0.1:27017
        serverAddresses = new ArrayList<>();
        int port = config.port().get() == null ? 27017 : config.port().get();
        if( config.nodes().get().isEmpty() )
        {
            String hostname = config.hostname().get() == null ? "127.0.0.1" : config.hostname().get();
            serverAddresses.add( new ServerAddress( hostname, port ) );
        }
        else
        {
            if( config.hostname().get() != null && !config.hostname().get().isEmpty() )
            {
                serverAddresses.add( new ServerAddress( config.hostname().get(), port ) );
            }
            serverAddresses.addAll( config.nodes().get() );
        }

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
    public MongoClient mongoInstanceUsed()
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
                        DBObject bsonState = (DBObject) JSON.parse( jsonState );

                        BasicDBObject entity = new BasicDBObject();
                        entity.put( IDENTITY_COLUMN, ref.identity() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.insert( entity, writeConcern );
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
                        entities.update( byIdentity( ref ), entity, false, false, writeConcern );
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
            public <ReceiverThrowableType extends Throwable> void transferTo(
                Output<? super Reader, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo(
                        Receiver<? super Reader, ReceiverThrowableType> receiver )
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
