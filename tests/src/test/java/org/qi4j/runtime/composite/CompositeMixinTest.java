/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package org.qi4j.runtime.composite;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test CompositeMixin implementation
 */
public class CompositeMixinTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( TestComposite.class );
    }

    @Test
    public void testGetCompositeType()
    {
        TestComposite composite = compositeBuilderFactory.newComposite( TestComposite.class );
        Class<? extends Composite> compositeType = composite.type();

        assertEquals( TestComposite.class, compositeType );
    }

    public interface TestComposite
        extends Composite
    {
    }
}
