package org.qi4j.runtime;
/*
* Copyright (C) Senselogic 2006, all rights reserved
*/

import junit.framework.*;
import org.qi4j.api.Composite;
import org.qi4j.api.ObjectFactory;
import org.qi4j.test.model.DomainInterface;
import org.qi4j.test.model.StandardThis;
import org.qi4j.test.model.StandardThat;
import org.qi4j.test.model.TestComposite;
import org.qi4j.test.model.StandardThisImpl;
import org.qi4j.test.model.Standard;
import org.qi4j.test.model.StandardThatImpl;
import org.qi4j.test.model.DomainInterfaceImpl;
import org.qi4j.test.model.CustomTestComposite;

public class CompositeTest extends TestCase
{
    Composite composite;

    protected void setUp() throws Exception
    {
        composite = new Composite( TestComposite.class );
    }

    public void testGetImplementation() throws Exception
    {
        assertNull( composite.getMixin( Standard.class));

        System.out.println(composite);

        assertEquals( DomainInterfaceImpl.class, composite.getMixin( DomainInterface.class).getFragmentClass());
        assertEquals( StandardThisImpl.class, composite.getMixin( StandardThis.class).getFragmentClass());
        assertEquals( StandardThatImpl.class, composite.getMixin( StandardThat.class).getFragmentClass());

        ObjectFactory factory = new ObjectFactoryImpl();

        {
            TestComposite object = factory.newInstance( TestComposite.class );

            assertEquals( "bar=foo:FOO Hello World", object.foo( "FOO "));

            object.setFoo( "xyz");
            try
            {
                object.setFoo( null );
                fail( "Should have thrown an exception");
            }
            catch( Exception e )
            {
                // Ok
            }
        }

        {
            TestComposite object = factory.newInstance( CustomTestComposite.class );

            object.setFoo( "xyz");
            assertEquals( "FOO:foo:xyz", object.getFoo());

            System.out.println(object.getComposite());

            object.setFoo( null );
        }
    }
}