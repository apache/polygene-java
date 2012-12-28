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

public class CargoArrivedException extends InspectionException
{
    public CargoArrivedException( HandlingEvent handlingEvent )
    {
        super( handlingEvent );
    }

    @Override
    public String getMessage()
    {
        msg = "Cargo '" + id + "' has arrived in destination " + location + ".";
        msg += "\nMOCKUP REQUEST TO CARGO OWNER: Please claim cargo '" + id + "' in " + city + ".";

        return msg;
    }
}