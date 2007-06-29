/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.test;

import org.qi4j.library.general.model.composites.AddressComposite;
import org.qi4j.library.general.model.composites.CityComposite;
import org.qi4j.library.general.model.composites.CountryComposite;
import org.qi4j.library.general.model.composites.StateComposite;

public class AddressCompositeTest extends AbstractTest
{
    public void testNewAddressCompositeSuccessful() throws Exception
    {
        AddressComposite addressComposite;
        addressComposite = compositeFactory.newInstance( AddressComposite.class );

        String firstLineAdd = "IOI Tower";
        String secondLineAdd = "101 Collins St.";
        String thirdLineAdd = null;
        String zipcode = "3000";
        String cityName = "Melbourne";
        String stateName = "Victoria";
        String countryName = "Australia";

        addressComposite.setFirstLine( firstLineAdd );
        addressComposite.setSecondLine( secondLineAdd );
        addressComposite.setThirdLine( thirdLineAdd );
        addressComposite.setZipCode( zipcode );

        CityComposite city = compositeFactory.newInstance( CityComposite.class );
        city.setName( cityName );
        addressComposite.setCity( city );

        StateComposite state = compositeFactory.newInstance( StateComposite.class );
        state.setName( "Victoria" );
        addressComposite.setState( state );

        CountryComposite country = compositeFactory.newInstance( CountryComposite.class );
        country.setIsoCode( "AU" );
        country.setName( "Australia" );
        addressComposite.setCountry( country );

        assertEquals( firstLineAdd, addressComposite.getFirstLine() );
        assertEquals( secondLineAdd, addressComposite.getSecondLine() );
        assertNull( addressComposite.getThirdLine() );
        assertEquals( zipcode, addressComposite.getZipCode() );

        CityComposite otherCity = addressComposite.getCity();

        assertEquals( city.getCompositeObject(), otherCity.getCompositeObject() );
        assertEquals( cityName, otherCity.getName() );

        StateComposite otherState = addressComposite.getState();
        assertEquals( state.getCompositeObject(), otherState.getCompositeObject() );
        assertEquals( stateName, otherState.getName() );

        CountryComposite otherCountry = addressComposite.getCountry();
        assertEquals( country.getCompositeObject(), otherCountry.getCompositeObject() );
        assertEquals( countryName, otherCountry.getName() );
    }
}
