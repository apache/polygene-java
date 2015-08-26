/**
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zest.library.eventsourcing.domain.factory;

import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

/**
 * DomainEventValue factory
 */
@Concerns( UnitOfWorkNotificationConcern.class )
@Mixins( DomainEventFactoryService.DomainEventFactoryMixin.class )
public interface DomainEventFactoryService
    extends DomainEventFactory, ServiceComposite
{
    class DomainEventFactoryMixin
        implements DomainEventFactory
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Override
        public DomainEventValue createEvent( EntityComposite entity, String name, Object[] args )
        {
            ValueBuilder<DomainEventValue> builder = vbf.newValueBuilder( DomainEventValue.class );

            DomainEventValue prototype = builder.prototype();
            prototype.name().set( name );
            prototype.entityType().set( ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( entity )
                                            .types()
                                            .findFirst()
                                            .get()
                                            .getName() );
            prototype.entityId().set( entity.identity().get() );

            // JSON-ify parameters
            JSONStringer json = new JSONStringer();
            try
            {
                JSONWriter params = json.object();
                for( int i = 0; i < args.length; i++ )
                {
                    params.key( "param" + i );
                    if( args[ i ] == null )
                    {
                        params.value( JSONObject.NULL );
                    }
                    else
                    {
                        params.value( args[ i ] );
                    }
                }
                json.endObject();
            }
            catch( JSONException e )
            {
                throw new IllegalArgumentException( "Could not create eventValue", e );
            }
            prototype.parameters().set( json.toString() );
            return builder.newInstance();
        }
    }
}
