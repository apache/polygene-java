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
package org.apache.zest.entitystore.hazelcast;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.UrlXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.service.ServiceActivation;
import org.apache.zest.io.Input;
import org.apache.zest.io.Output;
import org.apache.zest.io.Receiver;
import org.apache.zest.io.Sender;
import org.apache.zest.spi.entitystore.EntityNotFoundException;
import org.apache.zest.spi.entitystore.EntityStoreException;
import org.apache.zest.spi.entitystore.helpers.MapEntityStore;

/**
 * Hazelcast implementation of MapEntityStore.
 */
public class HazelcastEntityStoreMixin
    implements ServiceActivation, HazelcastAccessors, MapEntityStore
{

    private static final String DEFAULT_MAPNAME = "zest:entitystore:data";

    @This
    private Configuration<HazelcastConfiguration> config;

    private IMap<String, String> stringMap;
    private HazelcastInstance hazelcastInstance;

    @Override
    public void activateService()
        throws Exception
    {
        HazelcastConfiguration configuration = config.get();
        Config conf = createConfig( configuration );
        hazelcastInstance = Hazelcast.newHazelcastInstance( conf );
        String mapName = DEFAULT_MAPNAME;
        if( configuration != null && configuration.mapName() != null )
        {
            mapName = configuration.mapName().get();
        }
        stringMap = hazelcastInstance.getMap( mapName );
    }

    @Override
    public void passivateService()
        throws Exception
    {
        stringMap = null;
        hazelcastInstance.getLifecycleService().shutdown();
    }

    @Override
    public HazelcastInstance hazelcastInstanceUsed()
    {
        return hazelcastInstance;
    }

    @Override
    public IMap hazelcastMapUsed()
    {
        return stringMap;
    }

    @Override
    public Reader get( EntityReference ref )
        throws EntityStoreException
    {
        final String serializedState = stringMap.get( ref.identity().toString() );
        if( serializedState == null )
        {
            throw new EntityNotFoundException( ref );
        }
        return new StringReader( serializedState );
    }

    @Override
    public void applyChanges( MapChanges changes )
        throws IOException
    {
        changes.visitMap( new MapChanger()
        {

            @Override
            public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                return new StringWriter( 1000 )
                {

                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        stringMap.put( ref.identity().toString(), toString() );
                    }
                };
            }

            @Override
            public Writer updateEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws IOException
            {
                return newEntity( ref, entityDescriptor );
            }

            @Override
            public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                throws EntityNotFoundException
            {
                stringMap.remove( ref.identity().toString() );
            }
        } );
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
                    public <RTT extends Throwable> void sendTo( Receiver<? super Reader, RTT> receiver )
                        throws RTT, IOException
                    {
                        for( Map.Entry<String, String> eachEntry : stringMap.entrySet() )
                        {
                            receiver.receive( new StringReader( eachEntry.getValue() ) );
                        }
                    }
                } );
            }
        };
    }

    private Config createConfig( HazelcastConfiguration configuration )
        throws IOException
    {
        String hzConfLocation = configuration.configXmlLocation().get();
        if( hzConfLocation == null || hzConfLocation.isEmpty() )
        {
            hzConfLocation = "hazelcast-default.xml";
        }
        Config conf;
        if( hzConfLocation.contains( ":" ) )
        {
            conf = new UrlXmlConfig( hzConfLocation );
        }
        else
        {
            conf = new ClasspathXmlConfig( hzConfLocation );
        }
        return conf;
    }
}
