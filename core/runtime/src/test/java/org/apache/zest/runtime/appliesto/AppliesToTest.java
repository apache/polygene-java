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
package org.apache.zest.runtime.appliesto;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

/**
 * Test of the @AppliesTo annotation
 */
public class AppliesToTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SomeComposite.class );
    }

    @Test
    public void givenAnAppliesToWhenNoAnnotationExpectNoConcernInInvocationStack()
        throws Exception
    {
        Some some = transientBuilderFactory.newTransient( Some.class );
        Assert.assertEquals( ",", some.doStuff1() );
    }

    @Test
    public void givenAnAppliesToWhenAnnotationIsOnMixinTypeExpectConcernInInvocationStack()
        throws Exception
    {
        Some some = transientBuilderFactory.newTransient( Some.class );
        Assert.assertEquals( ",,..", some.doStuff2() );
    }

    @Test
    public void givenAnAppliesToWhenAnnotationIsOnMixinImplementationExpectConcernInInvocationStack()
        throws Exception
    {
        Some some = transientBuilderFactory.newTransient( Some.class );
        Assert.assertEquals( ",", some.doStuff1() );
        Assert.assertEquals( ",,..", some.doStuff2() );
        Assert.assertEquals( ",,,", some.doStuff3() );
    }

    @Concerns( MyConcern.class )
    @Mixins( SomeMixin.class )
    private interface SomeComposite
        extends Some, TransientComposite
    {
    }

    public static interface Some
    {
        String doStuff1();

        @Foo
        String doStuff2();

        String doStuff3();
    }

    @AppliesTo( Foo.class )
    public static class MyConcern
        extends ConcernOf<Some>
        implements Some
    {

        public String doStuff1()
        {
            return next.doStuff1() + ".";
        }

        public String doStuff2()
        {
            return next.doStuff2() + "..";
        }

        public String doStuff3()
        {
            return next.doStuff3() + "...";
        }
    }

    public static class SomeMixin
        implements Some
    {

        public String doStuff1()
        {
            return ",";
        }

        public String doStuff2()
        {
            return ",,";
        }

        @Foo
        public String doStuff3()
        {
            return ",,,";
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    private @interface Foo
    {
    }
}
