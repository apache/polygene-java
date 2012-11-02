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

import java.util.List;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * List of events for a single transaction. Events must always be consumed
 * in transaction units, in order to ensure that the result is consistent
 * with what happened in that transaction.
 */
public interface TransactionApplicationEvents
        extends ValueComposite
{
    // Timestamp when the events were stored in the EventStore
    // Note that if events are sent from one store to another this timestamp
    // is updated when it is re-stored

    Property<Long> timestamp();

    // List of events for this transaction

    @UseDefaults
    Property<List<ApplicationEvent>> events();
}
