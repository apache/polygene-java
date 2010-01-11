/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.jdbm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.ByteArrayComparator;
import jdbm.helper.ByteArraySerializer;
import jdbm.helper.LongSerializer;
import jdbm.helper.MRU;
import jdbm.helper.Serializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import jdbm.recman.CacheRecordManager;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.spi.service.ServiceDescriptor;

/**
 * JDBM implementation of SerializationStore
 */
public class JdbmEntityStoreMixin
    implements Activatable, MapEntityStore, DatabaseExport, DatabaseImport
{
    @This
    private ReadWriteLock lock;

    @This
    private Configuration<JdbmConfiguration> config;

    @Uses
    private ServiceDescriptor descriptor;

    private RecordManager recordManager;
    private BTree index;
    private Serializer serializer;

    // Activatable implementation

    @SuppressWarnings( { "ResultOfMethodCallIgnored" } )
    public void activate()
        throws Exception
    {
        String pathname = config.configuration().file().get();
        if( pathname == null )
        {
            pathname = System.getProperty( "user.dir" ) + "/qi4j/jdbmstore.data";
        }
        File dataFile = new File( pathname );
        File directory = dataFile.getAbsoluteFile().getParentFile();
        directory.mkdirs();
        String name = dataFile.getAbsolutePath();
        Properties properties = getProperties( config.configuration() );
        initialize( name, properties );
    }

    public void passivate()
        throws Exception
    {
        recordManager.close();
    }

    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        try
        {
            Long stateIndex = getStateIndex( entityReference.identity() );

            if( stateIndex == null )
            {
                throw new EntityNotFoundException( entityReference );
            }

            byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

            if( serializedState == null )
            {
                throw new EntityNotFoundException( entityReference );
            }

            return new StringReader( new String( serializedState, "UTF-8" ) );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public void applyChanges( MapChanges changes )
        throws IOException
    {
        try
        {
            changes.visitMap( new MapChanger()
            {
                public Writer newEntity( final EntityReference ref, EntityType entityType )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();

                            byte[] stateArray = toString().getBytes( "UTF-8" );
                            long stateIndex = recordManager.insert( stateArray, serializer );
                            String indexKey = ref.toString();
                            index.insert( indexKey.getBytes( "UTF-8" ), stateIndex, false );
                        }
                    };
                }

                public Writer updateEntity( final EntityReference ref, EntityType entityType )
                    throws IOException
                {
                    return new StringWriter( 1000 )
                    {
                        @Override
                        public void close()
                            throws IOException
                        {
                            super.close();

                            Long stateIndex = getStateIndex( ref.toString() );
                            byte[] stateArray = toString().getBytes( "UTF-8" );
                            recordManager.update( stateIndex, stateArray, serializer );
                        }
                    };
                }

                public void removeEntity( EntityReference ref, EntityType entityType )
                    throws EntityNotFoundException
                {
                    try
                    {
                        Long stateIndex = getStateIndex( ref.toString() );
                        recordManager.delete( stateIndex );
                        index.remove( ref.toString().getBytes( "UTF-8" ) );
                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException( e );
                    }
                }
            } );

            recordManager.commit();
        }
        catch( Exception e )
        {
            recordManager.rollback();
            if( e instanceof IOException )
            {
                throw (IOException) e;
            }
            else if( e instanceof EntityStoreException )
            {
                throw (EntityStoreException) e;
            }
            else
            {
                IOException exception = new IOException();
                exception.initCause( e );
                throw exception;
            }
        }
    }

    public void visitMap( MapEntityStoreVisitor visitor )
    {
        try
        {
            final TupleBrowser browser = index.browse();
            final Tuple tuple = new Tuple();

            while( browser.getNext( tuple ) )
            {
                String id = new String( (byte[]) tuple.getKey(), "UTF-8" );

                Long stateIndex = getStateIndex( id );

                if( stateIndex == null )
                {
                    throw new EntityNotFoundException( new EntityReference( id ) );
                }

                byte[] serializedState = (byte[]) recordManager.fetch( stateIndex, serializer );

                visitor.visitEntity( new StringReader( new String( serializedState, "UTF-8" ) ) );
            }
        }
        catch( IOException e )
        {
            throw new EntityStoreException( e );
        }
    }

    public void exportTo( Writer out )
        throws IOException
    {
        TupleBrowser browser = index.browse();
        Tuple tuple = new Tuple();
        while( browser.getNext( tuple ) )
        {
            Long stateIndex = (Long) tuple.getValue();
            byte[] bytes = (byte[]) recordManager.fetch( stateIndex, serializer );
            String value = new String( bytes, "UTF-8" );
            out.write( value );
            out.write( '\n' );
        }
    }

    public void importFrom( Reader in )
        throws IOException
    {
        BufferedReader reader = new BufferedReader( in );
        String object;
        try
        {
            while( ( object = reader.readLine() ) != null )
            {
                String id = object.substring( "{\"identity\":\"".length() );
                id = id.substring( 0, id.indexOf( '"' ) );
                Long stateIndex = getStateIndex( id );
                if( stateIndex == null )
                {
                    // Insert
                    byte[] stateArray = object.getBytes( "UTF-8" );
                    stateIndex = recordManager.insert( stateArray, serializer );
                    index.insert( id.getBytes( "UTF-8" ), stateIndex, false );
                }
                else
                {
                    byte[] stateArray = object.getBytes( "UTF-8" );
                    recordManager.update( stateIndex, stateArray, serializer );
                }
            }
            recordManager.commit();
        }
        catch( IOException ex )
        {
            recordManager.rollback();
            throw ex;
        }
        catch( Exception ex )
        {
            recordManager.rollback();
            throw (IOException) new IOException( "Could not import data" ).initCause( ex );
        }
    }

    private Properties getProperties( JdbmConfiguration config )
    {
        Properties properties = new Properties();

        properties.put( RecordManagerOptions.AUTO_COMMIT, config.autoCommit().get().toString() );
        properties.put( RecordManagerOptions.DISABLE_TRANSACTIONS, config.disableTransactions().get().toString() );

        return properties;
    }

    private Long getStateIndex( String identity )
        throws IOException
    {
        return (Long) index.find( identity.getBytes( "UTF-8" ) );
    }

    private void initialize( String name, Properties properties )
        throws IOException
    {
        recordManager = RecordManagerFactory.createRecordManager( name, properties );
        serializer = new ByteArraySerializer();
        recordManager = new CacheRecordManager( recordManager, new MRU( 1000 ) );
        long recid = recordManager.getNamedObject( "index" );
        if( recid != 0 )
        {
            index = BTree.load( recordManager, recid );
        }
        else
        {
            ByteArrayComparator comparator = new ByteArrayComparator();
            index = BTree.createInstance( recordManager, comparator, serializer, new LongSerializer(), 16 );
            recordManager.setNamedObject( "index", index.getRecid() );
        }
        recordManager.commit();
    }
}