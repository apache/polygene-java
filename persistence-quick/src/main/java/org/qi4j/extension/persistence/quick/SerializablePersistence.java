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
import java.util.List;
import java.util.Map;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.PersistenceException;
import org.qi4j.runtime.composite.AbstractCompositeInstance;
import org.qi4j.runtime.composite.CompositeInstance;
import org.qi4j.runtime.composite.ProxyReferenceInvocationHandler;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.serialization.SerializablePersistenceSpi;
import org.qi4j.spi.serialization.SerializedObject;

public final class SerializablePersistence
    //   implements EntityStore
{
    private SerializablePersistenceSpi delegate;
    private Qi4jSPI spi;

    public SerializablePersistence( SerializablePersistenceSpi aDelegate, Qi4jSPI spi )
    {
        this.spi = spi;
        delegate = aDelegate;
    }

    public void create( EntityComposite entity )
        throws PersistenceException
    {
        CompositeInstance handler = AbstractCompositeInstance.getCompositeInstance( entity );
        Object[] mixins = handler.getMixins();

        Map<Class, SerializedObject> persistentMixins = new HashMap<Class, SerializedObject>();
        handler.getContext().getCompositeModel().getMixinModels();
        for( Object mixin : mixins )
        {
            if( mixin instanceof Serializable )
            {
                persistentMixins.put( mixin.getClass(), new SerializedObject( mixin, spi ) );
            }
        }

        String id = entity.identity().get();
        delegate.putInstance( id, persistentMixins );
    }

    public void read( EntityComposite entity )
        throws PersistenceException
    {
        String id = entity.identity().get();
        Map<Class, SerializedObject> mixins = delegate.getInstance( id );
        CompositeInstance handler = AbstractCompositeInstance.getCompositeInstance( entity.dereference() );
        if( mixins == null )
        {
            throw new EntityCompositeNotFoundException( "Object not found", id, handler.getContext().getCompositeModel().getCompositeClass() );
        }

        ProxyReferenceInvocationHandler proxyHandler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( entity );
/* TODO Fix this code!
        Object[] deserializedMixins = handler.getMixins();
        for( Map.Entry<Class, SerializedObject> entry : mixins.entrySet() )
        {
            SerializedObject value = entry.getValue();
            Object deserializedMixin = null;
            try
            {
                deserializedMixin = value.getObject( entitySession, compositeBuilderFactory );
            }
            catch( ClassNotFoundException e )
            {
                throw new StoreException( e );
            }
            deserializedMixins.put( entry.getKey(), deserializedMixin );
        }

        try
        {
            proxyHandler.initializeMixins( deserializedMixins );
        }
        catch( Exception e )
        {
            throw new StoreException( e );
        }
*/
    }

    public void update( EntityComposite entity, Serializable aMixin )
        throws PersistenceException
    {
        ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( entity );

        String identity = entity.identity().get();
        Map<Class, SerializedObject> mixins = delegate.getInstance( identity );
        if( mixins != null )
        {
            Class mixinType = handler.getMixinType();
            SerializedObject oldValueObject = mixins.get( mixinType );

            // Only update if there already were a value object. Otherwise ignore.
            if( oldValueObject != null )
            {
                SerializedObject newValueObject = new SerializedObject( aMixin, spi );
                mixins.put( mixinType, newValueObject );
                delegate.putInstance( identity, mixins );
            }
        }
    }

    public void delete( EntityComposite entity )
        throws PersistenceException
    {
        String id = entity.identity().get();
        delegate.removeInstance( id );
    }

    // TODO: Re-thinking
    public EntityComposite getEntity( String anIdentity, Class aType )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO: Re-thinking
    public void putEntity( EntityComposite composite )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean exists( String identity ) throws org.qi4j.spi.entity.StoreException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityState newEntityInstance( String identity, CompositeModel compositeModel ) throws org.qi4j.spi.entity.StoreException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityState getEntityInstance( String identity, Class compositeType ) throws org.qi4j.spi.entity.StoreException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<EntityState> getEntityInstances( List<String> identities, CompositeModel compositeModel ) throws org.qi4j.spi.entity.StoreException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EntityState newEntityInstance( String identity, Class compositeType ) throws StoreException
    {
        return null;
    }

    public List<EntityState> getEntityInstances( List<String> identities, Class compositeType ) throws StoreException
    {
        return null;
    }

    public boolean delete( String identity ) throws org.qi4j.spi.entity.StoreException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
