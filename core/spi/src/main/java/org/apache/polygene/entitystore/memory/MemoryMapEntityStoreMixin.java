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
package org.apache.polygene.entitystore.memory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.spi.entitystore.BackupRestore;
import org.apache.polygene.spi.entitystore.EntityAlreadyExistsException;
import org.apache.polygene.spi.entitystore.EntityNotFoundException;
import org.apache.polygene.spi.entitystore.EntityStoreException;
import org.apache.polygene.spi.entitystore.helpers.JSONKeys;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStore;
import org.apache.polygene.spi.entitystore.helpers.MapEntityStoreActivation;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * In-memory implementation of MapEntityStore.
 */
public class MemoryMapEntityStoreMixin
    implements MapEntityStore, BackupRestore, MapEntityStoreActivation
{
    private final Map<EntityReference, String> store;

    public MemoryMapEntityStoreMixin()
    {
        store = new HashMap<>();
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
    public Stream<Reader> entityStates()
    {
        return store.values().stream().map( StringReader::new );
    }

    @Override
    public Stream<String> backup()
    {
        return store.values().stream();
    }

    @Override
    public void restore( final Stream<String> stream )
    {
        store.clear();
        stream.forEach( item -> {
            try
            {
                JSONTokener tokener = new JSONTokener( item );
                JSONObject entity = (JSONObject) tokener.nextValue();
                String id = entity.getString( JSONKeys.IDENTITY );
                store.put( EntityReference.parseEntityReference( id ), item );
            }
            catch( JSONException e )
            {
                throw new UncheckedIOException( new IOException( e ) );
            }
        } );
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
