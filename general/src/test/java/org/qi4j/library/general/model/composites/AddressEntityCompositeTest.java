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
package org.qi4j.library.general.model.composites;

import org.qi4j.library.general.model.AbstractTest;

public class AddressEntityCompositeTest extends AbstractTest
{
    public void testNewAddressCompositeSuccessful() throws Exception
    {
        AddressEntityComposite addressEntityComposite;
        addressEntityComposite = compositeFactory.newInstance( AddressEntityComposite.class );

        String firstLineAdd = "IOI Tower";
        String secondLineAdd = "101 Collins St.";
        String thirdLineAdd = null;
        String zipcode = "3000";
        String cityName = "Melbourne";
        String stateName = "Victoria";
        String countryName = "Australia";

        addressEntityComposite.setFirstLine( firstLineAdd );
        addressEntityComposite.setSecondLine( secondLineAdd );
        addressEntityComposite.setThirdLine( thirdLineAdd );
        addressEntityComposite.setZipCode( zipcode );

        CityEntityComposite city = compositeFactory.newInstance( CityEntityComposite.class );
        city.setName( cityName );
        addressEntityComposite.setCity( city );

        StateEntityComposite state = compositeFactory.newInstance( StateEntityComposite.class );
        state.setName( "Victoria" );
        city.setState( state );

        CountryEntityComposite country = compositeFactory.newInstance( CountryEntityComposite.class );
        country.setIsoCode( "AU" );
        country.setName( "Australia" );
        city.setCountry( country );

        assertEquals( firstLineAdd, addressEntityComposite.getFirstLine() );
        assertEquals( secondLineAdd, addressEntityComposite.getSecondLine() );
        assertNull( addressEntityComposite.getThirdLine() );
        assertEquals( zipcode, addressEntityComposite.getZipCode() );

        CityEntityComposite otherCity = (CityEntityComposite) addressEntityComposite.getCity();

        assertEquals( city.getCompositeModel(), otherCity.getCompositeModel() );
        assertEquals( cityName, otherCity.getName() );

        StateEntityComposite otherState = (StateEntityComposite) city.getState();
        assertEquals( state.getCompositeModel(), otherState.getCompositeModel() );
        assertEquals( stateName, otherState.getName() );

        CountryEntityComposite otherCountry = (CountryEntityComposite) city.getCountry();
        assertEquals( country.getCompositeModel(), otherCountry.getCompositeModel() );
        assertEquals( countryName, otherCountry.getName() );
    }
}
