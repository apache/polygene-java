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
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.CompositeRepository;
import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.model.MixinsArbitrator;
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
            throw new IllegalStateException( "This exception should not be possible.", e );
        }

    }

    public Object getObject( CompositeRepository repository, CompositeFactory factory )
        throws ClassNotFoundException
    {
        try
        {
            ByteArrayInputStream in = new ByteArrayInputStream( data );
            CompositeInputStream stream = new CompositeInputStream( in, repository, factory );
            return stream.readObject();
        }
        catch( IOException e )
        {
            // can not happen, as there is no underlying I/O to go wrong!
            throw new IllegalStateException( "This exception should not be possible.", e );
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
            if( obj instanceof Composite )
            {
                Composite composite = (Composite) obj;
                CompositeObject compositeObject = composite.getCompositeObject();
                Class compositeInterface = compositeObject.getCompositeInterface();
                if( obj instanceof PersistentComposite )
                {
                    String id = ( (PersistentComposite) composite ).getIdentity();
                    return new IdentityHolder( id, compositeInterface );
                }
                else
                {
                    Map<Class, Object> mixinsToSave = new HashMap<Class, Object>();
                    MixinsArbitrator mixinsHolder = (MixinsArbitrator) Proxy.getInvocationHandler( obj );
                    Map<Class, Object> existingMixins = mixinsHolder.getMixins();
                    for( Map.Entry<Class, Object> entry : existingMixins.entrySet() )
                    {
                        if( entry.getValue() instanceof Serializable )
                        {
                            mixinsToSave.put( entry.getKey(), entry.getValue() );
                        }
                    }
                    return new CompositeHolder( mixinsToSave, compositeInterface );
                }
            }
            return obj;
        }

    }

    private final class CompositeInputStream extends ObjectInputStream
    {
        private CompositeRepository repository;
        private CompositeFactory factory;

        public CompositeInputStream( InputStream in, CompositeRepository repository, CompositeFactory factory )
            throws IOException
        {
            super( in );
            this.factory = factory;
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
            if( obj instanceof CompositeHolder )
            {
                CompositeHolder holder = (CompositeHolder) obj;
                Class<Composite> compositeInterface = holder.getCompositeInterface();
                Map<Class, Object> mixins = holder.getMixins();
                Composite composite = factory.newInstance( compositeInterface );
                MixinsArbitrator mixinHandler = (MixinsArbitrator) Proxy.getInvocationHandler( composite );
                mixinHandler.setMixins( mixins );
            }
            return obj;
        }
    }

    private final class IdentityHolder
        implements Serializable
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

    private class CompositeHolder
        implements Serializable
    {
        private Map<Class, Object> mixins;
        private Class<Composite> compositeInterface;

        public CompositeHolder( Map<Class, Object> mixins, Class<Composite> compositeInterface )
        {
            this.mixins = mixins;
            this.compositeInterface = compositeInterface;
        }

        public Map<Class, Object> getMixins()
        {
            return mixins;
        }

        public Class<Composite> getCompositeInterface()
        {
            return compositeInterface;
        }
    }
}
