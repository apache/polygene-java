/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.qi4j.Composite;
import org.qi4j.CompositeBuilderFactory;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
import org.qi4j.spi.composite.CompositeState;

/**
 * TODO
 */
final class CompositeInputStream extends ObjectInputStream
{
    private EntitySession session;
    private CompositeBuilderFactory factory;

    public CompositeInputStream( InputStream in, EntitySession session, CompositeBuilderFactory factory )
        throws IOException
    {
        super( in );
        this.factory = factory;
        this.session = session;
        enableResolveObject( true );
    }

    protected Object resolveObject( Object obj ) throws IOException
    {
        if( obj instanceof SerializedEntity )
        {
            SerializedEntity holder = (SerializedEntity) obj;
            Class<EntityComposite> clazz = holder.getPersistentCompositeClass();
            String id = holder.getIdentity();
            Object instance = session.find( id, clazz );
            return instance;
        }

        if( obj instanceof SerializedComposite )
        {
            // TODO Fix this!!
            SerializedComposite holder = (SerializedComposite) obj;
            Class<Composite> compositeInterface = holder.getCompositeInterface();
            Map<Class, Object> mixins = holder.getMixins();
            Composite composite = factory.newCompositeBuilder( compositeInterface ).newInstance();
            CompositeState mixinHandler = (CompositeState) Proxy.getInvocationHandler( composite );
// TODO                mixinHandler.setMixins( mixins, false );
        }
        return obj;
    }
}
