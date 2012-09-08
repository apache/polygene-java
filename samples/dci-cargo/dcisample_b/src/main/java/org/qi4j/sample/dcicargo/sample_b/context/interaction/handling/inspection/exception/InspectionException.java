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

import java.text.SimpleDateFormat;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;

/**
 * Base exception for all variations of inspection.
 */
public class InspectionException extends Exception
{
    protected HandlingEvent handlingEvent;
    protected String id, registered, completion, type, city, unloc, location;
    protected String voyage = "";
    protected String msg = "";

    public InspectionException( HandlingEvent handlingEvent )
    {
        super();
        this.handlingEvent = handlingEvent;

        SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd" );
        id = handlingEvent.trackingId().get().id().get();
        registered = date.format( handlingEvent.registrationTime().get() );
        completion = date.format( handlingEvent.completionTime().get() );
        type = handlingEvent.handlingEventType().get().name();
        city = handlingEvent.location().get().name().get();
        unloc = handlingEvent.location().get().getCode();
        location = handlingEvent.location().get().getString();

        if( handlingEvent.voyage().get() != null )
        {
            voyage = handlingEvent.voyage().get().voyageNumber().get().number().get();
        }
    }

    public InspectionException( HandlingEvent handlingEvent, String message )
    {
        this( handlingEvent );
        msg = message;
    }

    public InspectionException( String s )
    {
        super( s );
    }
}