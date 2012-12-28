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
 * Thrown when trying to register a handling event for an nonRouted cargo.
 */
public class NonRoutedCargoException extends CannotRegisterHandlingEventException
{
    public NonRoutedCargoException( ParsedHandlingEventData parsedHandlingEventData )
    {
        super( parsedHandlingEventData );
    }

    @Override
    public String getMessage()
    {
        msg = "\nUnsuccessful handling event registration for cargo '" + id +
              "' (handling event '" + type + "' in '" + unloc + "'). Cargo is not routed!";

        msg += "\nMOCKUP NOTIFICATION TO HANDLING AUTHORITY: Please check submitted invalid handling event data:";
        msg += getParsedHandlingEventData();

        return msg;
    }
}