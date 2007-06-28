package org.qi4j.library.general.test;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.qi4j.api.CompositeFactory;
import org.qi4j.library.general.model.composites.AddressComposite;
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
        addressComposite.setCityName( cityName );
        addressComposite.setStateName( stateName );
        addressComposite.setCountryName( countryName );

        Assert.assertEquals( firstLineAdd, addressComposite.getFirstLine() );
        Assert.assertEquals( secondLineAdd, addressComposite.getSecondLine() );
        assertNull( addressComposite.getThirdLine() );
        Assert.assertEquals( zipcode, addressComposite.getZipCode() );
        Assert.assertEquals( cityName, addressComposite.getCityName() );
        Assert.assertEquals( stateName, addressComposite.getStateName() );
        Assert.assertEquals( countryName, addressComposite.getCountryName() );
    }
}
