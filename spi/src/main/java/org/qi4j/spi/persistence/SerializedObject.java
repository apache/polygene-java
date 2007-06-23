/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.spi.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeRepository;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.persistence.composite.PersistentComposite;

public class SerializedObject
    implements Serializable
{
    private byte[] data;

    public SerializedObject( Object value )
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompositeOutputStream stream = new CompositeOutputStream( out );
            stream.writeObject( value );
            stream.flush();
            data = out.toByteArray();
            stream.close();
            out.close();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
        }

    }

    public Object getObject( CompositeRepository repository )
        throws ClassNotFoundException
    {
        try
        {
            ByteArrayInputStream in = new ByteArrayInputStream( data );
            CompositeInputStream stream = new CompositeInputStream( in, repository );
            return stream.readObject();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            return null;
        }
    }

    private final class CompositeOutputStream extends ObjectOutputStream
    {
        public CompositeOutputStream( OutputStream out )
            throws IOException
        {
            super( out );
            enableReplaceObject( true );
        }

        protected Object replaceObject( Object obj ) throws IOException
        {
            if( obj instanceof PersistentComposite )
            {
                PersistentComposite composite = (PersistentComposite) obj;
                String id = composite.getIdentity();
                CompositeObject compositeObject = composite.getCompositeObject();
                Class compositeInterface = compositeObject.getCompositeInterface();
                return new IdentityHolder( id, compositeInterface );
            }
            return obj;
        }

    }

    private final class CompositeInputStream extends ObjectInputStream
    {
        private CompositeRepository repository;

        public CompositeInputStream( InputStream in, CompositeRepository repository )
            throws IOException
        {
            super( in );
            this.repository = repository;
            enableResolveObject( true );
        }

        protected Object resolveObject( Object obj ) throws IOException
        {
            if( obj instanceof IdentityHolder )
            {
                IdentityHolder holder = (IdentityHolder) obj;
                Class<PersistentComposite> clazz = holder.getPersistentCompositeClass();
                String id = holder.getIdentity();
                Object instance = repository.getInstance( id, clazz );
                return instance;
            }
            return obj;
        }
    }

    private final class IdentityHolder
    {
        private String identity;
        private Class<PersistentComposite> clazz;

        public IdentityHolder( String identity, Class<PersistentComposite> clazz )
        {
            this.identity = identity;
            this.clazz = clazz;
        }

        public String getIdentity()
        {
            return identity;
        }

        public Class<PersistentComposite> getPersistentCompositeClass()
        {
            return clazz;
        }
    }
}
