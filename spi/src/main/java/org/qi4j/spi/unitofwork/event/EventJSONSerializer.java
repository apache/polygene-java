/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.unitofwork.event;

/**
 * Serialize entity events to JSON
 */
public final class EventJSONSerializer
{
    String toJSON( UnitOfWorkEvent event )
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "{" );

        if( event instanceof EntityEvent )
        {
            EntityEvent entityEvent = (EntityEvent) event;
            builder.append( "on:" + entityEvent.identity().identity() );

            if( entityEvent instanceof EntityStateEvent )
            {
                EntityStateEvent entityStateEvent = (EntityStateEvent) entityEvent;
                builder.append( ",for:" ).append( entityStateEvent.stateName().qualifiedName() );

                if( entityStateEvent instanceof SetPropertyEvent )
                {
                    SetPropertyEvent setPropertyEvent = (SetPropertyEvent) entityStateEvent;
                    builder.append( ",set:" ).append( setPropertyEvent.value() );

                }
            }
        }

        builder.append( "}" );

        return builder.toString();
    }
}
