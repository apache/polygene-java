/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.envisage.school.domain.model.person;

import static org.qi4j.api.util.NullArgumentException.validateNotNull;

import java.io.Serializable;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class PersonId
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String personId;

    public PersonId( String aPersonId )
        throws IllegalArgumentException
    {
        validateNotNull( "aPersonId", aPersonId );
        personId = aPersonId;
    }

    public final String idString()
    {
        return personId;
    }
}