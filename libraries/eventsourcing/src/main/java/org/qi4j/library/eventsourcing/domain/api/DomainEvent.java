/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.eventsourcing.domain.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate methods that should trigger domain events with this annotation. Example:
 * <pre>
 * &#64;DomainEvent
 * void changedDescription(String newDescription);
 * </pre>
 * Event methods may only change state. They may not fail or thrown exceptions. The name of the
 * method should be in past tense, as in something HAS already occurred, and the method is merely
 * reacting to it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEvent
{
}
