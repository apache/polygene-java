package org.qi4j.runtime;
/*
* Copyright (C) Senselogic 2006, all rights reserved
*/

import junit.framework.*;
import org.qi4j.runtime.Composite;

public class CompositeTest extends TestCase
{
    Composite composite;

    protected void setUp() throws Exception
    {
        composite = new Composite( TestComposite.class );
    }

    public void testGetImplementation() throws Exception
    {
        assertNull( composite.getImplementation( Standard.class));

        System.out.println(composite);

        assertEquals( DomainInterfaceImpl.class, composite.getImplementation( DomainInterface.class));
        assertEquals( StandardThisImpl.class, composite.getImplementation( StandardThis.class));
        assertEquals( StandardThatImpl.class, composite.getImplementation( StandardThat.class));
    }
}