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

package org.qi4j.library.eventsourcing.domain.source;

import java.io.IOException;
import org.qi4j.io.Output;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;

/**
 * Store of domain-events.
 */
public interface EventStore
{
    Output<UnitOfWorkDomainEventsValue, IOException> storeEvents();
}
