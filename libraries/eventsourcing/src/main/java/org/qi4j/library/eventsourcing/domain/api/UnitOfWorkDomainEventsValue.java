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

package org.qi4j.library.eventsourcing.domain.api;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * List of events for a single UnitOfWork. Events must always be consumed
 * in UoW units, in order to ensure that the result is consistent
 * with what happened in that UoW.
 *
 * Context that is common to all events in the UoW is stored here rather than
 * in the individual event.
 */
public interface UnitOfWorkDomainEventsValue
        extends ValueComposite
{
    // Version of the application that created these events
    Property<String> version();

    // Usecase name
    Property<String> usecase();

    // When the event occurred
    Property<Long> timestamp();

    // Who performed the event. Taken from CurrentUser service.
    @Optional
    Property<String> user();

    // List of events for this transaction
    @UseDefaults
    Property<List<DomainEventValue>> events();
}
