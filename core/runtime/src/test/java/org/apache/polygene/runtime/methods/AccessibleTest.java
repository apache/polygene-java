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

import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AccessibleTest extends AbstractPolygeneTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( MyComposite.class );
    }

    @Test
    public void givenPrivateMixinWhenCallingFromWithinExpectSuccess()
    {
        MyComposite myComposite = transientBuilderFactory.newTransient( MyComposite.class );
        assertThat(myComposite.doSomething(), equalTo("Hello"));
    }

    @Mixins( MyCompositeMixin.class)
    public interface MyComposite
    {
        String doSomething();
    }

    @Mixins( MyFunctionMixin.class )
    public interface MyFunction
    {
        String doSomething();
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

    public class MyFunctionMixin
        implements MyFunction
    {
        @Override
        public String doSomething()
        {
            return "Hello " + getClass().getClassLoader();
        }
    }

    public class MyObject
    {
        private MyFunction fn;

        public MyObject( MyFunction fn )
        {
            this.fn = fn;
        }

        String doSomething()
        {
            return fn.doSomething();
        }
    }
}
