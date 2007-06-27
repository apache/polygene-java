package org.qi4j.library.general.test;

import junit.framework.TestCase;
import junit.framework.Assert;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.runtime.CompositeFactoryImpl;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.GenderType;
import org.qi4j.library.general.model.Person;

public class PersonTest extends TestCase
{
    private CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }

    public void testNewPerson() throws Exception
    {
        PersonComposite person = compositeFactory.newInstance( PersonComposite.class );

        String firstName = "Sianny";
        String lastName = "Halim";

        person.setFirstName( firstName );
        person.setLastName( lastName );
        person.setGender( GenderType.female );

        Assert.assertEquals( firstName, person.getFirstName() );
        Assert.assertEquals( lastName, person.getLastName() );
        Assert.assertEquals( GenderType.female, person.getGender() );
    }

    @ImplementedBy( { PropertiesMixin.class } )
    private interface PersonComposite extends Person, Composite
    {
    }

}
