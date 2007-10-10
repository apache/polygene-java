package org.qi4j.runtime;

import junit.framework.TestCase;
import org.qi4j.api.CompositeBuilderFactory;

public abstract class AbstractTest extends TestCase
{
    protected CompositeBuilderFactory builderFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        builderFactory = new CompositeBuilderFactoryImpl();
    }
}
