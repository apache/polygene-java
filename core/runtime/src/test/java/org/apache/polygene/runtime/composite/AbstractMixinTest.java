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

package org.apache.polygene.runtime.composite;

import java.lang.reflect.Method;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.concern.GenericConcern;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;

/**
 * JAVADOC
 */
public class AbstractMixinTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        System.setProperty( "polygene.compacttrace", "off" );
        module.transients( TestComposite.class );
    }

    @Test
    public void testAbstractMixin()
    {
        TestComposite instance = transientBuilderFactory.newTransient( TestComposite.class );

        Assert.assertEquals( "Hello WorldHello World", instance.test( "Hello World" ) );
    }

    public interface TestComposite
        extends TestInterface, TransientComposite
    {
    }

    @Mixins( TestComposite.TestMixin.class )
    @Concerns( { TestInterface.TestConcern.class, TestInterface.TestAbstractConcern.class } )
    public interface TestInterface
    {
        String test( String newValue );

        interface TestState
        {
            @Optional
            Property<String> bar();

            void barChanged( String newValue );
        }

        abstract class TestMixin
            implements TestInterface, TestState
        {
            public void init( @Structure Module module )
            {
                System.out.println( module );
            }

            public String test( String newValue )
            {
                newValue = duplicate( newValue );
                barChanged( newValue );
                return bar().get();
            }

            public void barChanged( String newValue )
            {
                bar().set( newValue );
            }

            public String duplicate( String newValue )
            {
                return newValue + newValue;
            }
        }

        public class TestConcern
            extends GenericConcern
        {
            public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable
            {
                System.out.println( method.toGenericString() );
                return next.invoke( proxy, method, args );
            }
        }

        public abstract class TestAbstractConcern
            extends ConcernOf<TestState>
            implements TestState
        {
            public void barChanged( String newValue )
            {
                next.barChanged( newValue );
                System.out.println( "Concern:" + bar().get() );
            }
        }
    }
}
