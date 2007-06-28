package org.qi4j.library.general.test;

import junit.framework.TestCase;
import org.qi4j.api.CompositeFactory;
import org.qi4j.library.general.model.composites.AddressComposite;
import org.qi4j.library.general.model.composites.CityComposite;
import org.qi4j.library.general.model.composites.CountryComposite;
import org.qi4j.library.general.model.composites.StateComposite;
import org.qi4j.runtime.CompositeFactoryImpl;

public class AddressCompositeTest extends TestCase
{
    private CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }

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
