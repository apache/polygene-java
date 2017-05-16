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
package org.apache.polygene.entitystore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.Collectors.toList;

/**
 * MongoDB implementation of MapEntityStore.
 */
public class MongoMapEntityStoreMixin
    implements ServiceActivation, MapEntityStore, MongoAccessors
{
    private static final String DEFAULT_DATABASE_NAME = "polygene:entitystore";
    private static final String DEFAULT_COLLECTION_NAME = "polygene:entitystore:entities";
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
    private MongoDatabase db;

    @Override
    public void activateService()
        throws Exception
    {
        loadConfiguration();

        // Create Mongo driver and open the database
        MongoClientOptions options = MongoClientOptions.builder().writeConcern( writeConcern ).build();
        if( username.isEmpty() )
        {
            mongo = new MongoClient( serverAddresses, options );
        }
        else
        {
            MongoCredential credential = MongoCredential.createMongoCRCredential( username, databaseName, password );
            mongo = new MongoClient( serverAddresses, Collections.singletonList( credential ), options );
        }
        db = mongo.getDatabase( databaseName );

        // Create index if needed
        MongoCollection<Document> entities = db.getCollection( collectionName );
        if( !entities.listIndexes().iterator().hasNext() )
        {
            entities.createIndex( new BasicDBObject( IDENTITY_COLUMN, 1 ) );
        }
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
        List<String> nodes = config.nodes().get();
        if( nodes.isEmpty() )
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
            serverAddresses.addAll( nodes.stream()
                                         .map( this::parseNode )
                                         .collect( toList() )
                                  );
        }

        // If database name not configured, set it to polygene:entitystore
        databaseName = config.database().get();
        if( databaseName == null )
        {
            databaseName = DEFAULT_DATABASE_NAME;
        }

        // If collection name not configured, set it to polygene:entitystore:entities
        collectionName = config.collection().get();
        if( collectionName == null )
        {
            collectionName = DEFAULT_COLLECTION_NAME;
        }

        // If write concern not configured, set it to normal
        switch( config.writeConcern().get() )
        {
        case W1:
            writeConcern = WriteConcern.W1;
            break;
        case W2:
            writeConcern = WriteConcern.W2;
            break;
        case W3:
            writeConcern = WriteConcern.W3;
            break;
        case UNACKNOWLEDGED:
            writeConcern = WriteConcern.UNACKNOWLEDGED;
            break;
        case JOURNALED:
            writeConcern = WriteConcern.JOURNALED;
            break;
        case MAJORITY:
            writeConcern = WriteConcern.MAJORITY;
            break;
        case ACKNOWLEDGED:
        default:
            writeConcern = WriteConcern.ACKNOWLEDGED;
        }

        // Username and password are defaulted to empty strings
        username = config.username().get();
        password = config.password().get().toCharArray();
    }

    private <R> ServerAddress parseNode( String nodeString )
    {
        String[] parts = nodeString.split( ":" );
        String host = parts[ 0 ];
        if( parts.length == 2 )
        {
            int port = Integer.parseInt( parts[ 1 ] );
            return new ServerAddress( host, port );
        }
        return new ServerAddress( host );
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
    public MongoDatabase dbInstanceUsed()
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
        MongoCursor<Document> cursor = db.getCollection( collectionName )
                                         .find( byIdentity( entityReference ) )
                                         .limit( 1 ).iterator();
        if( !cursor.hasNext() )
        {
            throw new EntityNotFoundException( entityReference );
        }
        Document bsonState = (Document) cursor.next().get( STATE_COLUMN );
        String jsonState = JSON.serialize( bsonState );
        return new StringReader( jsonState );
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws Exception
    {
        final MongoCollection<Document> entities = db.getCollection( collectionName );

        changes.visitMap( new MapChanger()
        {
            @Override
            public Writer newEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse( toString() );
                        Document entity = new Document();
                        entity.put( IDENTITY_COLUMN, ref.identity().toString() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.insertOne( entity );
                    }
                };
            }

            @Override
            public Writer updateEntity( MapChange mapChange )
                throws IOException
            {
                return new StringWriter( 1000 )
                {
                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        Document bsonState = Document.parse( toString() );
                        Document entity = new Document();
                        entity.put( IDENTITY_COLUMN, mapChange.reference().identity().toString() );
                        entity.put( STATE_COLUMN, bsonState );
                        entities.replaceOne( byIdentity( mapChange.reference() ), entity );
                    }
                };
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws EntityNotFoundException
            {
                Bson byIdFilter = byIdentity( ref );
                MongoCursor<Document> cursor = db.getCollection( collectionName )
                                                 .find( byIdFilter )
                                                 .limit( 1 ).iterator();
                if( !cursor.hasNext() )
                {
                    throw new EntityNotFoundException( ref );
                }
                entities.deleteOne( byIdFilter );
            }
        } );
    }

    @Override
    public Stream<Reader> entityStates()
    {
        return StreamSupport
            .stream( db.getCollection( collectionName ).find().spliterator(), false )
            .map( eachEntity ->
                  {
                      Document bsonState = (Document) eachEntity.get( STATE_COLUMN );
                      String jsonState = JSON.serialize( bsonState );
                      return new StringReader( jsonState );
                  } );
    }

    private Bson byIdentity( EntityReference entityReference )
    {
        return eq( IDENTITY_COLUMN, entityReference.identity().toString() );
    }
}
