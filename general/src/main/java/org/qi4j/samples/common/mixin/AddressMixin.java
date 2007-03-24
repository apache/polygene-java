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
package org.ops4j.orthogon.samples.common.mixin;

public final class AddressMixin
    implements Address
{
    private String m_street;
    private String m_city;
    private String m_zipcode;
    private String m_country;

    public AddressMixin()
    {
    }

    public final String getCity()
    {
        return m_city;
    }

    public final void setCity( String city )
    {
        m_city = city;
    }

    public final String getCountry()
    {
        return m_country;
    }

    public final void setCountry( String country )
    {
        m_country = country;
    }

    public final String getStreetAddress()
    {
        return m_street;
    }

    public final String getZipCode()
    {
        return m_zipcode;
    }

    public final void setStreetAddress( String street )
    {
        m_street = street;
    }

    public final void setZipCode( String zipcode )
    {
        m_zipcode = zipcode;
    }
}
