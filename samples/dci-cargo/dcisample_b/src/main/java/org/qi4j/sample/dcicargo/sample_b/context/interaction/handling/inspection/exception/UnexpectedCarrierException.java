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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.inspection.exception;

import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;

public class UnexpectedCarrierException extends InspectionException
{
    public UnexpectedCarrierException( HandlingEvent handlingEvent )
    {
        super( createMessage(handlingEvent) );
    }

    private static String createMessage( HandlingEvent handlingEvent )
    {
        String voyage = handlingEvent.voyage().get().voyageNumber().get().number().get();
        String city = handlingEvent.location().get().name().get();
        String location = handlingEvent.location().get().getString();
        return "\nCarrier of voyage " + voyage + " didn't expect a load in " + location;
    }
}