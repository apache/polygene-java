package org.qi4j.entitystore.memory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;
import org.qi4j.spi.entitystore.helpers.MapEntityStoreActivation;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore, BackupRestore, MapEntityStoreActivation
{
    private final Map<EntityReference, String> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<EntityReference, String>();
    }

    @Override
    public void activateMapEntityStore()
        throws Exception
    {
        // NOOP
    }

    public boolean contains( EntityReference entityReference, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        return store.containsKey( entityReference );
    }

    @Override
    public Reader get( EntityReference entityReference )
        throws EntityStoreException
    {
        String state = store.get( entityReference );
        if( state == null )
        {
            throw new EntityNotFoundException( entityReference );
        }

        return new StringReader( state );
    }

    @Override
    public void applyChanges( MapEntityStore.MapChanges changes )
        throws IOException
    {
        changes.visitMap( new MemoryMapChanger() );
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
                        for( String state : store.values() )
                        {
                            receiver.receive( new StringReader( state ) );
                        }
                    }
                } );
            }
        };
    }

    @Override
    public Input<String, IOException> backup()
    {
        return new Input<String, IOException>()
        {
            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super String, ReceiverThrowableType> output )
                throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<String, IOException>()
                {
                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super String, ReceiverThrowableType> receiver )
                        throws ReceiverThrowableType, IOException
                    {
                        for( String state : store.values() )
                        {
                            receiver.receive( state );
                        }
                    }
                } );
            }
        };
    }

    @Override
    public Output<String, IOException> restore()
    {
        return new Output<String, IOException>()
        {
            @Override
            public <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends String, SenderThrowableType> sender )
                throws IOException, SenderThrowableType
            {
                store.clear();

                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        @Override
                        public void receive( String item )
                            throws IOException
                        {
                            try
                            {
                                JSONTokener tokener = new JSONTokener( item );
                                JSONObject entity = (JSONObject) tokener.nextValue();
                                String id = entity.getString( JSONKeys.identity.name() );
                                store.put( new EntityReference( id ), item );
                            }
                            catch( JSONException e )
                            {
                                throw new IOException( e );
                            }
                        }
                    } );
                }
                catch( IOException e )
                {
                    store.clear();
                    throw e;
                }
            }
        };
    }

    private class MemoryMapChanger
        implements MapChanger
    {
        @Override
        public Writer newEntity( final EntityReference ref, EntityDescriptor descriptor )
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    String old = store.put( ref, toString() );
                    if( old != null )
                    {
                        store.put( ref, old );
                        throw new EntityAlreadyExistsException( ref );
                    }
                }
            };
        }

        @Override
        public Writer updateEntity( final EntityReference ref, EntityDescriptor descriptor )
            throws IOException
        {
            return new StringWriter( 1000 )
            {
                @Override
                public void close()
                    throws IOException
                {
                    super.close();
                    String old = store.put( ref, toString() );
                    if( old == null )
                    {
                        store.remove( ref );
                        throw new EntityNotFoundException( ref );
                    }
                }
            };
        }

        @Override
        public void removeEntity( EntityReference ref, EntityDescriptor descriptor )
            throws EntityNotFoundException
        {
            String state = store.remove( ref );
            // Ignore if the entity didn't already exist, as that can happen if it is both created and removed
            // within the same UnitOfWork.
//            if( state == null )
//            {
//                throw new EntityNotFoundException( ref );
//            }
        }
    }
}