/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.extension.persistence.quick;

import org.qi4j.api.ObjectFactory;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.persistence.ObjectNotFoundException;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.binding.PersistenceBinding;
import org.qi4j.api.persistence.modifier.PersistentStorageReferenceModifier;
import org.qi4j.api.persistence.modifier.PersistentStorageTraceModifier;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;
import org.qi4j.spi.persistence.SerializablePersistenceSpi;
import org.qi4j.runtime.ObjectInvocationHandler;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;

@ModifiedBy( { PersistentStorageTraceModifier.class, PersistentStorageReferenceModifier.class } )
public final class SerializablePersistence
    implements PersistentStorage
{
    SerializablePersistenceSpi delegate;
    private ObjectFactory objectFactory;

    public SerializablePersistence( SerializablePersistenceSpi aDelegate, ObjectFactory objectFactory )
    {
        delegate = aDelegate;
        this.objectFactory = objectFactory;
    }

    public void create( PersistenceBinding aProxy )
        throws PersistenceException
    {
        ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( aProxy );
        Map<Class, Object> mixins = handler.getMixins();

        Map<Class, MarshalledObject> persistentMixins = new HashMap<Class, MarshalledObject>();
        for( Map.Entry<Class, Object> entry : mixins.entrySet() )
        {
            if( entry.getValue() instanceof Serializable )
            {
                try
                {
                    persistentMixins.put( entry.getKey(), new MarshalledObject( entry.getValue() ) );
                }
                catch( IOException e )
                {
                    // TODO Better message
                    throw new PersistenceException( e );
                }
            }
        }

        String id = aProxy.getIdentity();
        delegate.putInstance( id, persistentMixins );
    }

    public void read( PersistenceBinding aProxy )
        throws PersistenceException
    {
        String id = aProxy.getIdentity();
        Map<Class, MarshalledObject> mixins = delegate.getInstance( id );
        if( mixins == null )
        {
            throw new ObjectNotFoundException( "Object with identity " + id + " does not exist" );
        }

        ProxyReferenceInvocationHandler proxyHandler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( aProxy );
        ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( objectFactory.getThat( aProxy ) );
        Map<Class, Object> deserializedMixins = handler.getMixins();
        for( Map.Entry<Class, MarshalledObject> entry : mixins.entrySet() )
        {
            MarshalledObject value = entry.getValue();

            try
            {
                Object deserializedMixin = value.get();
                deserializedMixins.put( entry.getKey(), deserializedMixin );
            }
            catch( Exception e )
            {
                // TODO Better message
                throw new PersistenceException( e );
            }
        }

        try
        {
            // TODO Improve?
            Object currentMixin = deserializedMixins.get( proxyHandler.getMixinType() );
            Object invokedMixin = proxyHandler.getMixin();
            BeanInfo info = Introspector.getBeanInfo( currentMixin.getClass() );
            PropertyDescriptor[] properties = info.getPropertyDescriptors();
            for( PropertyDescriptor property : properties )
            {
                Method read = property.getReadMethod();
                Method write = property.getWriteMethod();
                if( read != null && write != null )
                {
                    Object value = property.getReadMethod().invoke( currentMixin, new Object[0] );
                    Method writeMethod = property.getWriteMethod();
                    writeMethod.invoke( invokedMixin, value );
                }
            }
        }
        catch( Exception e )
        {
            throw new PersistenceException( e );
        }
    }

    public void update( PersistenceBinding aProxy, Object aMixin )
        throws PersistenceException
    {
        ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( aProxy );

        String identity = aProxy.getIdentity();
        Map<Class, MarshalledObject> mixins = delegate.getInstance( identity );
        if( mixins != null )
        {
            try
            {
                Class mixinType = handler.getMixinType();
                MarshalledObject oldValueObject = mixins.get( mixinType );

                // Only update if there already were a value object. Otherwise ignore.
                if( oldValueObject != null )
                {
                    MarshalledObject newValueObject = new MarshalledObject( aMixin );
                    mixins.put( mixinType, newValueObject );
                    delegate.putInstance( identity, mixins );
                }
            }
            catch( IOException e )
            {
                // TODO Better message
                throw new PersistenceException( e );
            }
        }
    }

    public void delete( PersistenceBinding aProxy )
        throws PersistenceException
    {
        String id = aProxy.getIdentity();
        delegate.removeInstance( id );
    }

}
