/**
 *
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.source.jdbm;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.Serializer;
import jdbm.btree.BTree;
import jdbm.helper.ByteArrayComparator;
import jdbm.helper.DefaultSerializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import jdbm.recman.CacheRecordManager;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.functional.Function;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.io.Transforms;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.source.AbstractEventStoreMixin;
import org.qi4j.library.eventsourcing.domain.source.EventManagement;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.qi4j.library.eventsourcing.domain.source.EventStore;
import org.qi4j.library.eventsourcing.domain.source.EventStoreActivation;
import org.qi4j.library.eventsourcing.domain.source.EventStream;
import org.qi4j.library.fileconfig.FileConfiguration;

/**
 * JAVADOC
 */
@Mixins( JdbmEventStoreService.JdbmEventStoreMixin.class )
@Activators( EventStoreActivation.Activator.class )
public interface JdbmEventStoreService
    extends EventSource, EventStore, EventStream, EventManagement, EventStoreActivation, ServiceComposite
{

    class JdbmEventStoreMixin
        extends AbstractEventStoreMixin
        implements EventManagement, EventSource
    {
        @Service
        private FileConfiguration fileConfig;

        @Service
        @Tagged( ValueSerialization.Formats.JSON )
        private ValueSerialization valueSerialization;

        private RecordManager recordManager;
        private BTree index;
        private Serializer serializer;
        private File dataFile;

        private long currentCount;

        @Override
        public void activateEventStore()
            throws Exception
        {
            super.activateEventStore();
            dataFile = new File( fileConfig.dataDirectory(), identity.identity() + "/events" );
            File directory = dataFile.getAbsoluteFile().getParentFile();
            directory.mkdirs();
            String name = dataFile.getAbsolutePath();
            Properties properties = new Properties();
            properties.put( RecordManagerOptions.AUTO_COMMIT, "false" );
            properties.put( RecordManagerOptions.DISABLE_TRANSACTIONS, "false" );
            initialize( name, properties );
        }

        @Override
        public void passivateEventStore()
                throws Exception
        {
            super.passivateEventStore();
            recordManager.close();
        }

        @Override
        public Output<String, IOException> restore()
        {
            // Commit every 1000 events, convert from string to value, and then store. Put a lock around the whole thing
            Output<String, IOException> map = Transforms.map( new Transforms.ProgressLog<String>( 1000 )
            {
                @Override
                protected void logProgress()
                {
                    try
                    {
                        recordManager.commit(); // Commit every 1000 transactions to avoid OutOfMemory issues
                    }
                    catch( IOException e )
                    {
                        throw new IllegalStateException( "Could not commit data", e );
                    }
                }
            }, Transforms.map( new Function<String, UnitOfWorkDomainEventsValue>()
            {
                @Override
                public UnitOfWorkDomainEventsValue map( String item )
                {
                    return valueSerialization.<UnitOfWorkDomainEventsValue>deserialize( eventsType, item );
                }
            }, storeEvents0() ) );

            return Transforms.lock( JdbmEventStoreMixin.this.lock,
                                    map );
        }

        // EventStore implementation
        @Override
        public Input<UnitOfWorkDomainEventsValue, IOException> events( final long offset, long limit )
        {
            return new Input<UnitOfWorkDomainEventsValue, IOException>()
            {
                @Override
                public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> output )
                    throws IOException, ReceiverThrowableType
                {
                    output.receiveFrom( new Sender<UnitOfWorkDomainEventsValue, IOException>()
                    {
                        @Override
                        public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super UnitOfWorkDomainEventsValue, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
                        {
                            // Lock datastore first
                            lock();

                            try
                            {
                                final TupleBrowser browser = index.browse( offset + 1 );

                                Tuple tuple = new Tuple();

                                while( browser.getNext( tuple ) )
                                {
                                    // Get next transaction
                                    UnitOfWorkDomainEventsValue domainEvents = readTransactionEvents( tuple );

                                    receiver.receive( domainEvents );
                                }
                            }
                            catch( Exception e )
                            {
                                logger.warn( "Could not iterate events", e );
                            }
                            finally
                            {
                                lock.unlock();
                            }
                        }
                    } );
                }
            };
        }

        @Override
        public long count()
        {
            return currentCount;
        }

        @Override
        protected Output<UnitOfWorkDomainEventsValue, IOException> storeEvents0()
        {
            return new Output<UnitOfWorkDomainEventsValue, IOException>()
            {
                @Override
                public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends UnitOfWorkDomainEventsValue, SenderThrowableType> sender )
                    throws IOException, SenderThrowableType
                {
                    try
                    {
                        sender.sendTo( new Receiver<UnitOfWorkDomainEventsValue, IOException>()
                        {
                            @Override
                            public void receive( UnitOfWorkDomainEventsValue item )
                                throws IOException
                            {
                                String jsonString = valueSerialization.serialize( item );
                                currentCount++;
                                index.insert( currentCount, jsonString.getBytes( "UTF-8" ), false );
                            }
                        } );
                        recordManager.commit();
                    }
                    catch( IOException e )
                    {
                        recordManager.rollback();
                        throw e;
                    }
                    catch( Throwable e )
                    {
                        recordManager.rollback();
                        throw (SenderThrowableType) e;
                    }
                }
            };
        }

        private void initialize( String name, Properties properties )
            throws IOException
        {
            recordManager = RecordManagerFactory.createRecordManager( name, properties );
            serializer = DefaultSerializer.INSTANCE;
            recordManager = new CacheRecordManager( recordManager, 1000, false );
            long recid = recordManager.getNamedObject( "index" );
            if( recid != 0 )
            {
                index = BTree.load( recordManager, recid );
                currentCount = index.size();
            }
            else
            {
                ByteArrayComparator comparator = new ByteArrayComparator();
                index = BTree.createInstance( recordManager, comparator, serializer, DefaultSerializer.INSTANCE, 16 );
                recordManager.setNamedObject( "index", index.getRecid() );
                currentCount = 0;
            }
            recordManager.commit();
        }

        private UnitOfWorkDomainEventsValue readTransactionEvents( Tuple tuple )
            throws UnsupportedEncodingException
        {
            byte[] eventData = (byte[]) tuple.getValue();
            String eventJson = new String( eventData, "UTF-8" );
            return valueSerialization.<UnitOfWorkDomainEventsValue>deserialize( eventsType, eventJson );
        }
    }
}
