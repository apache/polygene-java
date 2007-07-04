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

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.EntityRepository;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.persistence.EntityCompositeNotFoundException;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.composite.EntityComposite;
import org.qi4j.api.persistence.modifier.PersistentStorageReferenceModifier;
import org.qi4j.api.persistence.modifier.PersistentStorageTraceModifier;
import org.qi4j.runtime.CompositeInvocationHandler;
import org.qi4j.runtime.ProxyReferenceInvocationHandler;
import org.qi4j.spi.persistence.SerializablePersistenceSpi;
import org.qi4j.spi.persistence.SerializedObject;

@ModifiedBy( { PersistentStorageTraceModifier.class, PersistentStorageReferenceModifier.class } )
public final class SerializablePersistence
    implements PersistentStorage
{
    SerializablePersistenceSpi delegate;
    private CompositeFactory compositeFactory;
    private EntityRepository entityRepository;

    public SerializablePersistence( SerializablePersistenceSpi aDelegate, CompositeFactory compositeFactory, EntityRepository entityRepository )
    {
        delegate = aDelegate;
        this.compositeFactory = compositeFactory;
        this.entityRepository = entityRepository;
    }

    public void create( EntityComposite entity )
        throws PersistenceException
    {
        CompositeInvocationHandler handler = CompositeInvocationHandler.getInvocationHandler( entity );
        Map<Class, Object> mixins = handler.getMixins();

        Map<Class, SerializedObject> persistentMixins = new HashMap<Class, SerializedObject>();
        for( Map.Entry<Class, Object> entry : mixins.entrySet() )
        {
            if( entry.getValue() instanceof Serializable )
            {
                persistentMixins.put( entry.getKey(), new SerializedObject( entry.getValue() ) );
            }
        }

        String id = entity.getIdentity();
        delegate.putInstance( id, persistentMixins );
    }

    public void read( EntityComposite entity )
        throws PersistenceException
    {
        String id = entity.getIdentity();
        Map<Class, SerializedObject> mixins = delegate.getInstance( id );
        if( mixins == null )
        {
            throw new EntityCompositeNotFoundException( "Object with identity " + id + " does not exist" );
        }

        ProxyReferenceInvocationHandler proxyHandler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( entity );
        CompositeInvocationHandler handler = CompositeInvocationHandler.getInvocationHandler( compositeFactory.dereference( entity ) );
        Map<Class, Object> deserializedMixins = handler.getMixins();
        for( Map.Entry<Class, SerializedObject> entry : mixins.entrySet() )
        {
            SerializedObject value = entry.getValue();
            Object deserializedMixin = null;
            try
            {
                deserializedMixin = value.getObject( entityRepository, compositeFactory );
            }
            catch( ClassNotFoundException e )
            {
                throw new PersistenceException( e );
            }
            deserializedMixins.put( entry.getKey(), deserializedMixin );
        }

        try
        {
            proxyHandler.initializeMixins( deserializedMixins );
        }
        catch( Exception e )
        {
            throw new PersistenceException( e );
        }
    }

    public void update( EntityComposite entity, Serializable aMixin )
        throws PersistenceException
    {
        ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( entity );

        String identity = entity.getIdentity();
        Map<Class, SerializedObject> mixins = delegate.getInstance( identity );
        if( mixins != null )
        {
            Class mixinType = handler.getMixinType();
            SerializedObject oldValueObject = mixins.get( mixinType );

            // Only update if there already were a value object. Otherwise ignore.
            if( oldValueObject != null )
            {
                SerializedObject newValueObject = new SerializedObject( aMixin );
                mixins.put( mixinType, newValueObject );
                delegate.putInstance( identity, mixins );
            }
        }
    }

    public void delete( EntityComposite entity )
        throws PersistenceException
    {
        String id = entity.getIdentity();
        delegate.removeInstance( id );
    }

}
