/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.runtime.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.zest.api.event.ZestEvent;
import org.apache.zest.api.event.ZestEventHandler;
import org.apache.zest.bootstrap.handler.ModuleAssembledEvent;

public class EventBus
{
    public EventBus() {
        ModuleAssembledRuntimeEvent.Handler handler = event -> EventBus.this.emit( new ModuleAssembledEvent( event.getModuleAssembly() ) );
        addHandler( ModuleAssembledRuntimeEvent.TYPE, handler );
    }

    /**
     * Map of event type to map of event source to list of their handlers.
     */
    private final Map<ZestEvent.Type<?>, List<?>> map =
        new HashMap<>();


    public <H extends ZestEventHandler> void addHandler(ZestEvent.Type<H> type, H handler) {
        if (type == null) {
            throw new NullPointerException("Cannot add a handler with a null type");
        }
        if (handler == null) {
            throw new NullPointerException("Cannot add a null handler");
        }

        // safe, we control the puts.
        @SuppressWarnings("unchecked")
        List<H> handlers = (List<H>) map.get( type );
        if (handlers == null) {
            handlers = new ArrayList<>();
            map.put( type, handlers);
        }

        handlers.add( handler );
    }

    public <H extends ZestEventHandler> void emit( ZestEvent<H> event) {
        if (event == null) {
            throw new NullPointerException("Cannot fire null event");
        }
        List<H> handlers = getDispatchList(event.getAssociatedType());

        ListIterator<H> it = handlers.listIterator();
        while (it.hasNext()) {
            H handler = it.next();
            event.dispatch(handler);
        }
    }

    private <H extends ZestEventHandler> List<H> getDispatchList(ZestEvent.Type<H> type) {
        // safe, we control the puts.
        @SuppressWarnings("unchecked")
        List<H> handlers = (List<H>) map.get( type );
        if (handlers == null) {
            return Collections.emptyList();
        }

        return handlers;
    }
}
