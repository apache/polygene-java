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

package org.apache.polygene.index.reindexer.internal;

import java.util.ArrayList;
import java.util.stream.Stream;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.index.reindexer.Reindexer;
import org.apache.polygene.index.reindexer.ReindexerConfiguration;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexerMixin
    implements Reindexer
{
    @This
    private Configuration<ReindexerConfiguration> configuration;

    @Service
    private EntityStore store;

    @Service
    private Iterable<ServiceReference<StateChangeListener>> listeners;

    @Structure
    private ModuleDescriptor module;

    private Logger logger = LoggerFactory.getLogger( Reindexer.class );

    @Override
    public void reindex()
    {
        configuration.refresh();
        ReindexerConfiguration conf = configuration.get();
        Integer loadValue = conf.loadValue().get();
        if( loadValue == null )
        {
            loadValue = 50;
        }
        ReindexerHelper helper = new ReindexerHelper( loadValue );
        helper.reindex( store );
    }

    private class ReindexerHelper
    {
        private int count;
        private int loadValue;
        private ArrayList<EntityState> states;

        private ReindexerHelper( int loadValue )
        {
            this.loadValue = loadValue;
            states = new ArrayList<>();
        }

        private void reindex( EntityStore store )
        {
            try( Stream<EntityState> entityStates = store.entityStates( module ) )
            {
                entityStates
                    .forEach( entityState ->
                              {
                                  count++;
                                  entityState.setPropertyValue( HasIdentity.IDENTITY_STATE_NAME,
                                                                entityState.entityReference().identity() );
                                  states.add( entityState );
                                  if( states.size() >= loadValue )
                                  {
                                      reindexState();
                                  }
                              } );
            }
            reindexState();
        }

        private void reindexState()
        {
            for( ServiceReference<StateChangeListener> listener : listeners )
            {
                listener.get().notifyChanges( states );
            }
            states.clear();
            logger.debug( "Reindexed " + count + " entities" );
        }
    }
}
