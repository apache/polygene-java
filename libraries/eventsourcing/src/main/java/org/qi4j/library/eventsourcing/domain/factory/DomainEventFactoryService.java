/**
 *
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

package org.qi4j.library.eventsourcing.domain.factory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;

/**
 * DomainEventValue factory
 */
@Concerns(UnitOfWorkNotificationConcern.class)
@Mixins(DomainEventFactoryService.DomainEventFactoryMixin.class)
public interface DomainEventFactoryService
        extends DomainEventFactory, ServiceComposite
{
    class DomainEventFactoryMixin
            implements DomainEventFactory
    {
        @Structure
        private ValueBuilderFactory vbf;

        public DomainEventValue createEvent( EntityComposite entity, String name, Object[] args )
        {
            ValueBuilder<DomainEventValue> builder = vbf.newValueBuilder( DomainEventValue.class );

            DomainEventValue prototype = builder.prototype();
            prototype.name().set( name );
            prototype.entityType().set( Qi4j.DESCRIPTOR_FUNCTION.map( entity ).type().getName() );
            prototype.entityId().set( entity.identity().get() );

            // JSON-ify parameters
            JSONStringer json = new JSONStringer();
            try
            {
                JSONWriter params = json.object();
                for (int i = 0; i < args.length; i++)
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
                throw new IllegalArgumentException( "Could not create eventValue", e );
            }

            prototype.parameters().set( json.toString() );

            DomainEventValue eventValue = builder.newInstance();

            return eventValue;
        }
    }
}
