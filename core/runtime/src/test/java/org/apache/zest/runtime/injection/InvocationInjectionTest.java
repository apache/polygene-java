/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.zest.runtime.injection;

import java.lang.annotation.Retention;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.injection.scope.Invocation;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Test the @Invocation annotation
 */
public class InvocationInjectionTest
{
    @Test
    public void whenInvocationInjectionWithMethodWhenInjectedThenInjectMethod()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.transients( MyComposite.class );
            }
        };

        MyComposite composite = assembly.module().newTransient( MyComposite.class );

        composite.doStuff();
        composite.doStuff();
        composite.doStuff2();
        composite.doStuff3();
    }

    @Mixins( MyMixin.class )
    @Concerns( MyConcern.class )
// START SNIPPET: declaration
    public interface MyComposite
        extends TransientComposite
    {
        @Foo( "1" )
        void doStuff();
// END SNIPPET: declaration
        void doStuff2();

        @Foo( "X" )
        void doStuff3();
    }

// START SNIPPET: use1
    public abstract static class MyConcern
        extends ConcernOf<MyComposite>
        implements MyComposite
    {
        @Invocation
        Foo foo;
// END SNIPPET: use1
        @Invocation
        Method method;

        @Invocation
        AnnotatedElement ae;

        public void doStuff()
        {
            Assert.assertThat( "interface has been injected", foo.value(), CoreMatchers.equalTo( "1" ) );
            Assert.assertThat( "annotations have been injected", ae.getAnnotation( Foo.class )
                .value(), CoreMatchers.equalTo( "1" ) );
            Assert.assertThat( "Method has been injected", method.getName(), CoreMatchers.equalTo( "doStuff" ) );
            next.doStuff();
        }

        public void doStuff2()
        {
            Assert.assertThat( "mixin has been injected", foo.value(), CoreMatchers.equalTo( "2" ) );
            Assert.assertThat( "annotations have been injected", ae.getAnnotation( Foo.class )
                .value(), CoreMatchers.equalTo( "2" ) );
            Assert.assertThat( "Method has been injected", method.getName(), CoreMatchers.equalTo( "doStuff2" ) );
            next.doStuff2();
        }

        public void doStuff3()
        {
            Assert.assertThat( "mixin has overridden interface", foo.value(), CoreMatchers.equalTo( "3" ) );
            Assert.assertThat( "annotations have been injected", ae.getAnnotation( Foo.class )
                .value(), CoreMatchers.equalTo( "3" ) );
            Assert.assertThat( "Method has been injected", method.getName(), CoreMatchers.equalTo( "doStuff3" ) );
            next.doStuff3();
        }
    }

    public abstract static class MyMixin
        implements MyComposite
    {
        public void doStuff()
        {
        }

        @Foo( "2" )
        public void doStuff2()
        {
        }

        @Foo( "3" )
        public void doStuff3()
        {
        }
    }
// START SNIPPET: annotation
    @Retention( RUNTIME )
    @interface Foo
    {
        String value();
    }
// END SNIPPET: annotation
}