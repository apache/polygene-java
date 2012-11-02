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

package org.qi4j.library.eventsourcing.application.api;

import java.util.Date;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Representation of an application-event. An application event is triggered by calling a method
 * that is of the form:
 * <p/>
 * void someName(ApplicationEvent event, SomeParam param);
 * <p/>
 * The "event" argument should be invoked with null, as it will be created during
 * the method call. If it is not null, then the method call is a replay of previously
 * created events.
 */
public interface ApplicationEvent
        extends ValueComposite, Identity
{
    // Usecase
    Property<String> usecase();

    // Name of method/event
    Property<String> name();

    // When the event was created
    Property<Date> on();

    // Method parameters as JSON
    Property<String> parameters();

    // Version of the application that created this event
    Property<String> version();
}
