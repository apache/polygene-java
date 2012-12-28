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

package org.qi4j.library.eventsourcing.domain.source;

import java.io.IOException;
import org.qi4j.io.Input;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

/**
 * An EventSource is a source of events. Events are grouped in the UnitOfWork in which they were created.
 */
public interface EventSource
{
    /**
     * Get list of UnitOfWorkDomainEventsValue after the given offset. To get the first set of events, use 0 as offset parameter to get events from the start.
     * <p/>
     *
     * @param offset where in the list of events to start
     * @param limit maximum number of events returned
     */
    Input<UnitOfWorkDomainEventsValue, IOException> events( long offset, long limit );

    long count();
}
