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
package org.apache.polygene.runtime.methods;

import java.lang.reflect.UndeclaredThrowableException;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public class AccessibleTest extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( MyComposite.class );
    }

    @Test
    public void givenPackageReturnTypeWhenCallingFromWithinExpectSuccess()
    {
        MyComposite myComposite = transientBuilderFactory.newTransient( MyComposite.class );
        try
        {
            myComposite.doSomething();
            fail("Should have gotten an IllegalAccessException");
        } catch( UndeclaredThrowableException e )
        {
            Throwable thrown = e.getUndeclaredThrowable();
            assertThat( thrown, instanceOf(IllegalAccessException.class));
        }
    }

    @Mixins( MyCompositeMixin.class)
    public interface MyComposite
    {
        String doSomething();
    }

    @Mixins( MyFunctionMixin.class )
    public interface MyFunction
    {
        MyString doSomething();
    }

    public class MyCompositeMixin
        implements MyComposite
    {
        @This
        private MyFunction function;

        @Override
        public String doSomething()
        {
            return new MyObject( function ).doSomething() + " ---- " + getClass().getClassLoader();
        }

    }

    public static class MyFunctionMixin
        implements MyFunction
    {
        @Override
        public MyString doSomething()
        {
            return new MyString( "Hello " );
        }
    }

    public static class MyObject
    {
        private MyFunction fn;

        public MyObject( MyFunction fn )
        {
            this.fn = fn;
        }

        String doSomething()
        {
            return fn.doSomething().mine;
        }
    }

    static class MyString
    {
        String mine;

        public MyString( String mine )
        {
            this.mine = mine;
        }
    }

}
