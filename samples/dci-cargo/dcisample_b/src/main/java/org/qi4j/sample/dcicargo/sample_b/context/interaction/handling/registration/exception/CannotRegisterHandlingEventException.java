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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.registration.exception;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;
import org.qi4j.sample.dcicargo.sample_b.data.structure.handling.HandlingEvent;

/**
 * If a {@link HandlingEvent} can't be created from a given set of parameters.
 *
 * It is a checked exception because it's not a programming error, but rather a
 * special case that the application is built to handle. It can occur during normal
 * program execution.
 */
public class CannotRegisterHandlingEventException extends Exception
{
    private ParsedHandlingEventData parsedHandlingEventData;

    protected String msg, id, time, type, unloc;
    protected String voy = "";

    public CannotRegisterHandlingEventException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super();
        this.parsedHandlingEventData = parsedHandlingEventData;

        time = parseDate( parsedHandlingEventData.completionTime().get() );
        id = parse( parsedHandlingEventData.trackingIdString().get() );
        type = parse( parsedHandlingEventData.handlingEventType().get().name() );
        unloc = parse( parsedHandlingEventData.unLocodeString().get() );
        voy = parse( parsedHandlingEventData.voyageNumberString().get() );
    }

    public ParsedHandlingEventData getParsedHandlingEventData()
    {
        return parsedHandlingEventData;
    }

    private String parse( String str )
    {
        return str == null ? "null" : str;
    }

    private String parseDate( Date date )
    {
        return date == null ? "null" : new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( date );
    }
}