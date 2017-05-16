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
 */
package org.apache.polygene.runtime.composite;

import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert that interface static methods are ignored when assembling composites.
 */
public class InterfaceStaticMethodsTest extends AbstractPolygeneTest
{
    public interface StaticMethods
    {
        @UseDefaults( "foo" )
        Property<String> foo();

        static String bar()
        {
            return "bar";
        }
    }

    public interface OverrideStaticMethods extends StaticMethods
    {
        static String bar()
        {
            return "bar overridden";
        }
    }

    @Override
    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        module.transients( StaticMethods.class, OverrideStaticMethods.class );
    }

    @Test
    public void staticMethods() throws NoSuchMethodException
    {
        StaticMethods staticMethods = transientBuilderFactory.newTransient( StaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( StaticMethods.bar(), equalTo( "bar" ) );
    }

    @Test
    public void overrideStaticMethods()
    {
        OverrideStaticMethods staticMethods = transientBuilderFactory.newTransient( OverrideStaticMethods.class );

        assertThat( staticMethods.foo().get(), equalTo( "foo" ) );
        assertThat( OverrideStaticMethods.bar(), equalTo( "bar overridden" ) );
    }
}
