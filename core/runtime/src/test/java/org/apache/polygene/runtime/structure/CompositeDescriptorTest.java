/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.structure;

import org.apache.polygene.api.composite.CompositeDescriptor;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class CompositeDescriptorTest
    extends AbstractPolygeneTest
{
    @Test
    public final void testCompositeDescriptorWithComposite()
        throws Throwable
    {
        // Test with Standard composite
        AddressComposite address = transientBuilderFactory.newTransient( AddressComposite.class );
        CompositeDescriptor addressDescriptor = spi.compositeDescriptorFor( address );

        assertThat( addressDescriptor, notNullValue() );
        assertThat( addressDescriptor.types().findFirst().orElse( null ), equalTo( AddressComposite.class ) );
        assertThat( TransientDescriptor.class.isAssignableFrom( addressDescriptor.getClass() ), is( true ) );
    }

    @Test
    public final void testTransientCompositeDescriptorWithComposite()
        throws Throwable
    {
        // Test with Standard composite
        AddressComposite address = transientBuilderFactory.newTransient( AddressComposite.class );
        TransientDescriptor addressDescriptor = spi.transientDescriptorFor( address );

        assertThat( addressDescriptor, notNullValue() );
        assertThat( addressDescriptor.types().findFirst().orElse( null ), equalTo( AddressComposite.class ) );
        assertThat( TransientDescriptor.class.isAssignableFrom( addressDescriptor.getClass() ), is( true ) );
    }

    @Test
    public final void testCompositeDescriptorWithMixin()
    {
        // Test with composite
        TransientDescriptor addressDesc = module.transientDescriptor( AddressComposite.class.getName() );
        assertThat( addressDesc, notNullValue() );

        assertThat( addressDesc.types().findFirst().orElse( null ), equalTo( AddressComposite.class ) );
    }

    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.transients( AddressComposite.class );
    }

    private interface AddressComposite
        extends Address, TransientComposite
    {
    }

    private interface Address
    {
    }
}