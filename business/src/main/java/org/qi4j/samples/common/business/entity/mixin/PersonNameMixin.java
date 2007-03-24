/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.samples.common.business.entity.mixin;

public final class PersonNameMixin
    implements PersonName
{
    private String m_firstName;
    private String m_lastName;

    public PersonNameMixin()
    {
    }

    public String getFirstName()
    {
        return m_firstName;
    }

    public void setFirstName( String name )
    {
        m_firstName = name;
    }

    public String getLastName()
    {
        return m_lastName;
    }

    public void setLastName( String lastName )
    {
        m_lastName = lastName;
    }
}
