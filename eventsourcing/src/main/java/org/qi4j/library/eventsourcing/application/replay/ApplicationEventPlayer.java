/*
 * Copyright 2009-2010 Streamsource AB
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

package org.qi4j.library.eventsourcing.application.replay;

import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;

/**
 * Service that can replay ApplicationEvents.
 */
public interface ApplicationEventPlayer
{
    /**
     * Invoke a domain event on a particular object. The object could
     * be the original object, but could also be a service that wants
     * to be invoked to handle the event.
     *
     * @param applicationEvent
     * @param object
     * @throws ApplicationEventReplayException
     *
     */
    public void playEvent( ApplicationEvent applicationEvent, Object object )
            throws ApplicationEventReplayException;
}