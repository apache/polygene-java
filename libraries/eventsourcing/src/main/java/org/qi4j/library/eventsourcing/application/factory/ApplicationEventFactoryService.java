/*
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.application.factory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;
import org.qi4j.library.eventsourcing.domain.factory.UnitOfWorkNotificationConcern;
import org.qi4j.library.eventsourcing.domain.spi.CurrentUser;

import java.util.Date;

/**
 * DomainEventValue factory
 */
@Concerns(UnitOfWorkNotificationConcern.class)
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
