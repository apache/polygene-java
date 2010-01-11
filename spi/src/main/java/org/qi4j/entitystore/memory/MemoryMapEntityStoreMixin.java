package org.qi4j.entitystore.memory;

import java.io.BufferedReader;
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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityAlreadyExistsException;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore, TestData
{
    private final Map<EntityReference, String> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<EntityReference, String>();
    }

    public boolean contains( EntityReference entityReference, EntityType entityType )
        throws EntityStoreException
    {
        return store.containsKey( entityReference );
    }

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

    public void applyChanges( MapEntityStore.MapChanges changes )
        throws IOException
    {
        changes.visitMap( new MemoryMapChanger() );
    }

    public void visitMap( MapEntityStoreVisitor visitor )
    {
        for( String state : store.values() )
        {
            visitor.visitEntity( new StringReader( state ) );
        }
    }

    public String exportData()
    {
        StringBuilder export = new StringBuilder();
        for( String entity : store.values() )
        {
            export.append( entity ).append( '\n' );
        }

        return export.toString();
    }

    public void importData( String data )
        throws IOException
    {
        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( data ) );
            String line;
            while( ( line = reader.readLine() ) != null )
            {
                JSONTokener tokener = new JSONTokener( line );
                JSONObject entity = (JSONObject) tokener.nextValue();
                String id = entity.getString( JSONKeys.identity.name() );
                store.put( new EntityReference( id ), line );
            }
        }
        catch( JSONException e )
        {
            throw (IOException) new IOException( "Could not import data" ).initCause( e );
        }
    }

    private class MemoryMapChanger
        implements MapChanger
    {
        public Writer newEntity( final EntityReference ref, EntityType entityType )
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
                    String old = store.put( ref, toString() );
                    if( old == null )
                    {
                        store.remove( ref );
                        throw new EntityNotFoundException( ref );
                    }
                }
            };
        }

        public void removeEntity( EntityReference ref, EntityType entityType )
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