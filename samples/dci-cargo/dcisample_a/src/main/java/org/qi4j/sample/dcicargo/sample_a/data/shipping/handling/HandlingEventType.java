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
package org.qi4j.sample.dcicargo.sample_a.data.shipping.handling;

/**
 * A handling event type either requires or prohibits a voyage association.
 */
public enum HandlingEventType
{
    RECEIVE( false ),
    LOAD( true ),
    UNLOAD( true ),
    CUSTOMS( false ),
    CLAIM( false );

    private final boolean voyageRequired;

    /**
     * Private enum constructor.
     *
     * @param voyageRequired whether or not a voyage is associated with this event type
     */
    private HandlingEventType( final boolean voyageRequired )
    {
        this.voyageRequired = voyageRequired;
    }

    /**
     * @return True if a voyage association is required for this event type.
     */
    public boolean requiresVoyage()
    {
        return voyageRequired;
    }

    /**
     * @return True if a voyage association is prohibited for this event type.
     */
    public boolean prohibitsVoyage()
    {
        return !requiresVoyage();
    }
}
