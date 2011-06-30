/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.samples.dddsample.domain.model.location;

import org.qi4j.samples.dddsample.domain.model.ValueObject;

import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * United nations location code.
 * <p/>
 * http://www.unece.org/cefact/locode/
 * http://www.unece.org/cefact/locode/DocColumnDescription.htm#LOCODE
 */
public class UnLocode
    implements ValueObject<UnLocode>
{
    private String unlocode;

    // Country code is exactly two letters.
    // Location code is usually three letters, but may contain the numbers 2-9 as well
    private static final Pattern VALID_PATTERN = Pattern.compile( "[a-zA-Z]{2}[a-zA-Z2-9]{3}" );

    /**
     * Constructor.
     *
     * @param countryAndLocation Location string.
     */
    public UnLocode( final String countryAndLocation )
    {
        notNull( countryAndLocation, "Country and location may not be null" );
        isTrue( VALID_PATTERN.matcher( countryAndLocation ).matches(),
                countryAndLocation + " is not a valid UN/LOCODE (does not match pattern)" );

        unlocode = countryAndLocation.toUpperCase();
    }

    /**
     * @return country code and location code concatenated, always upper case.
     */
    public String idString()
    {
        return unlocode;
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        UnLocode other = (UnLocode) o;

        return sameValueAs( other );
    }

    public boolean sameValueAs( UnLocode other )
    {
        return other != null && idString().equals( other.idString() );
    }

    @Override
    public int hashCode()
    {
        return unlocode.hashCode();
    }

    @Override
    public String toString()
    {
        return idString();
    }
}
