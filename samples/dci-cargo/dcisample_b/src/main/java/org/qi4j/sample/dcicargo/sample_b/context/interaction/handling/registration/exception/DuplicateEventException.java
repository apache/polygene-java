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

import org.qi4j.sample.dcicargo.sample_b.context.interaction.handling.parsing.dto.ParsedHandlingEventData;

/**
 * Thrown when trying to register a receive/in customs/claim handling event twice.
 */
public final class DuplicateEventException extends CannotRegisterHandlingEventException
{
    public DuplicateEventException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        if( type.equals( "RECEIVE" ) )
        {
            return "Cargo can't be received more than once.";
        }
        else if( type.equals( "CUSTOMS" ) )
        {
            return "Cargo can't be in customs more than once.";
        }
        else if( type.equals( "CLAIM" ) )
        {
            return "Cargo can't be claimed more than once.";
        }
        else
        {
            return "INTERNAL ERROR: Unexpected handling event type for this exception";
        }
    }
}
