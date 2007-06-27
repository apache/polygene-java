package org.qi4j.library.general.test;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.runtime.CompositeFactoryImpl;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.entities.AddressEntity;

public class AddressEntityTest extends TestCase
{
    private CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }

    public void testNewAddressEntitySuccessful() throws Exception
    {
        AddressComposite addressComposite;
        addressComposite = compositeFactory.newInstance( AddressComposite.class );

        String identity = "Address Identity";
        String firstLineAdd = "IOI Tower";
        String secondLineAdd = "101 Collins St.";
        String thirdLineAdd = null;
        String zipcode = "3000";
        String cityName = "Melbourne";
        String stateName = "Victoria";
        String countryName = "Australia";

        addressComposite.setIdentity( identity );
        addressComposite.setFirstLine( firstLineAdd );
        addressComposite.setSecondLine( secondLineAdd );
        addressComposite.setThirdLine( thirdLineAdd );
        addressComposite.setZipCode( zipcode );
        addressComposite.setCityName( cityName );
        addressComposite.setStateName( stateName );
        addressComposite.setCountryName( countryName );

        Assert.assertEquals( identity, addressComposite.getIdentity() );
        Assert.assertEquals( firstLineAdd, addressComposite.getFirstLine() );
        Assert.assertEquals( secondLineAdd, addressComposite.getSecondLine() );
        assertNull( addressComposite.getThirdLine() );
        Assert.assertEquals( zipcode, addressComposite.getZipCode() );
        Assert.assertEquals( cityName, addressComposite.getCityName() );
        Assert.assertEquals( stateName, addressComposite.getStateName() );
        Assert.assertEquals( countryName, addressComposite.getCountryName() );
    }

    public void testNewAddressEntityWithNullIdentity() throws Exception
    {
        AddressComposite addressComposite;
        addressComposite = compositeFactory.newInstance( AddressComposite.class );

        try
        {
            addressComposite.setIdentity( null );
            fail( "Identity should not be null." );
        }
        catch( NullPointerException e )
        {
            // Correct
        }
    }
    
    @ImplementedBy( { PropertiesMixin.class } )
    private interface AddressComposite extends Composite, AddressEntity
    {
    }
}
