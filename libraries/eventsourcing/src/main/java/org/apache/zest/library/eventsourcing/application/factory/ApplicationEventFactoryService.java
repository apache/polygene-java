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

package org.apache.zest.library.eventsourcing.application.factory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.entity.IdentityGenerator;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.eventsourcing.application.api.ApplicationEvent;
import org.apache.zest.library.eventsourcing.domain.spi.CurrentUser;

import java.util.Date;

/**
 * DomainEventValue factory
 */
@Concerns(TransactionNotificationConcern.class)
@Mixins(ApplicationEventFactoryService.Mixin.class)
public interface ApplicationEventFactoryService
        extends ApplicationEventFactory, ServiceComposite
{
    class Mixin
            implements ApplicationEventFactory
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Service
        IdentityGenerator idGenerator;

        @Service
        CurrentUser currentUser;

        String version;

        public void init( @Structure Application application )
        {
            version = application.version();
        }

        @Override
        public ApplicationEvent createEvent( String name, Object[] args )
        {
            ValueBuilder<ApplicationEvent> builder = vbf.newValueBuilder( ApplicationEvent.class );

            ApplicationEvent prototype = builder.prototype();
            prototype.name().set( name );
            prototype.on().set( new Date() );

            prototype.identity().set( idGenerator.generate( ApplicationEvent.class ) );

            UnitOfWork uow = uowf.currentUnitOfWork();
            prototype.usecase().set( uow.usecase().name() );
            prototype.version().set( version );

            // JSON-ify parameters
            JSONStringer json = new JSONStringer();
            try
            {
                JSONWriter params = json.object();
                for (int i = 1; i < args.length; i++)
                {
                    params.key( "param" + i );
                    if (args[i] == null)
                        params.value( JSONObject.NULL );
                    else
                        params.value( args[i] );
                }
                json.endObject();
            } catch (JSONException e)
            {
                throw new IllegalArgumentException( "Could not create event", e );
            }

            prototype.parameters().set( json.toString() );

            ApplicationEvent event = builder.newInstance();

            return event;
        }
    }
}
