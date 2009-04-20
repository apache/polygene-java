package org.qi4j.entitystore.memory;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.helpers.MapEntityStore;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore
{
    private final Map<EntityReference, byte[]> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<EntityReference, byte[]>();
    }

    public boolean contains( EntityReference entityReference, Usecase usecase, MetaInfo unitofwork ) throws EntityStoreException
    {
        return store.containsKey( entityReference );
    }

    public InputStream get( EntityReference entityReference, Usecase usecase, MetaInfo unitOfWork ) throws EntityStoreException
    {
        byte[] state = store.get( entityReference );
        if( state == null )
        {
            throw new EntityNotFoundException( entityReference );
        }

        return new ByteArrayInputStream( state );
    }

    public void applyChanges( MapChanges changes, Usecase usecase, MetaInfo unitOfWork ) throws IOException
    {
        changes.visitMap( new MapChanger()
        {
            public OutputStream newEntity( final EntityReference ref )
            {
                return new ByteArrayOutputStream()
                {
                    @Override public void close() throws IOException
                    {
                        super.close();
                        byte[] old = store.put( ref, toByteArray() );
                        if( old != null )
                        {
                            store.put( ref, old );
                            throw new EntityAlreadyExistsException( ref );
                        }
                    }
                };
            }

            public OutputStream updateEntity( final EntityReference ref ) throws IOException
            {
                return new ByteArrayOutputStream()
                {
                    @Override public void close() throws IOException
                    {
                        super.close();
                        byte[] old = store.put( ref, toByteArray() );
                        if( old == null )
                        {
                            store.remove( ref );
                            throw new EntityNotFoundException( ref );
                        }
                    }
                };
            }

            public void removeEntity( EntityReference ref ) throws EntityNotFoundException
            {
                byte[] state = store.remove( ref );
                if( state == null )
                {
                    throw new EntityNotFoundException( ref );
                }
            }
        }, usecase, unitOfWork );
    }

    public void visitMap( MapEntityStoreVisitor visitor, Usecase usecase, MetaInfo unitOfWorkMetaInfo )
    {
        for( byte[] bytes : store.values() )
        {
            visitor.visitEntity( new ByteArrayInputStream( bytes ) );
        }
    }
}