/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.context.support;

import java.util.Date;
import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.sample.dcicargo.sample_a.infrastructure.conversion.DTO;

/**
 * The RegisterHandlingEventAttemptDTO simply helps move event registration data around.
 */
@Immutable
public interface RegisterHandlingEventAttemptDTO extends DTO
{
    @Optional
    Property<Date> registrationTime();

    @Optional
    Property<Date> completionTime();

    @Optional
    Property<String> trackingIdString();

    @Optional
    Property<String> eventTypeString();

    @Optional
    Property<String> unLocodeString();

    @Optional
    Property<String> voyageNumberString();
}
