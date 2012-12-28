/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import org.junit.Test;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * JAVADOC
 */
public class QI256Test
    extends AbstractQi4jTest
{

    public static class TestException
        extends RuntimeException
    {
        public TestException()
        {
            super( "Test" );
        }
    }

    @Mixins( { TestTransient.TestTransientMixin.class } )
    public static interface TestTransient
        extends TransientComposite
    {
        public void declaredMethodThatThrowsCustomException();

        public void invokeDeclaredPublicMethodThatThrowsCustomException();

        public void invokePrivateMethodThatThrowsCustomException();

        public void invokeProtectedMethodThatThrowsCustomException();

        public void invokeUndeclaredPublicMethodThatThrowsCustomException();

        public void declaredMethodThatThrowsBuiltinInheritedException();

        public void invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();

        public void invokePrivateMethodThatThrowsBuiltinInheritedException();

        public void invokeProtectedMethodThatThrowsBuiltinInheritedException();

        public void invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();

        public void declaredMethodThatThrowsBuiltinException();

        public void invokeDeclaredPublicMethodThatThrowsBuiltinException();

        public void invokePrivateMethodThatThrowsBuiltinException();

        public void invokeProtectedMethodThatThrowsBuiltinException();

        public void invokeUndeclaredPublicMethodThatThrowsBuiltinException();

        public abstract class TestTransientMixin
            implements TestTransient
        {
            public void declaredMethodThatThrowsCustomException()
            {
                throw new TestException();
            }

            public void invokeDeclaredPublicMethodThatThrowsCustomException()
            {
                this.declaredMethodThatThrowsCustomException();
            }

            public void invokePrivateMethodThatThrowsCustomException()
            {
                this.privateThrowException( TestException.class );
            }

            public void invokeProtectedMethodThatThrowsCustomException()
            {
                this.protectedThrowException( TestException.class );
            }

            public void invokeUndeclaredPublicMethodThatThrowsCustomException()
            {
                this.publicThrowException( TestException.class );
            }

            public void declaredMethodThatThrowsBuiltinInheritedException()
            {
                throw new IllegalArgumentException();
            }

            public void invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException()
            {
                this.declaredMethodThatThrowsBuiltinInheritedException();
            }

            public void invokePrivateMethodThatThrowsBuiltinInheritedException()
            {
                this.privateThrowException( IllegalArgumentException.class );
            }

            public void invokeProtectedMethodThatThrowsBuiltinInheritedException()
            {
                this.protectedThrowException( IllegalArgumentException.class );
            }

            public void invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException()
            {
                this.publicThrowException( IllegalArgumentException.class );
            }

            public void declaredMethodThatThrowsBuiltinException()
            {
                throw new RuntimeException();
            }

            public void invokeDeclaredPublicMethodThatThrowsBuiltinException()
            {
                this.declaredMethodThatThrowsBuiltinException();
            }

            public void invokePrivateMethodThatThrowsBuiltinException()
            {
                this.privateThrowException( RuntimeException.class );
            }

            public void invokeProtectedMethodThatThrowsBuiltinException()
            {
                this.protectedThrowException( RuntimeException.class );
            }

            public void invokeUndeclaredPublicMethodThatThrowsBuiltinException()
            {
                this.publicThrowException( RuntimeException.class );
            }

            private void privateThrowException( Class<? extends RuntimeException> clazz )
            {
                try
                {
                    throw clazz.newInstance();
                }
                catch( Throwable t )
                {
                    if( clazz.isInstance( t ) )
                    {
                        throw clazz.cast( t );
                    }
                    // Ignore
                }
            }

            protected void protectedThrowException( Class<? extends RuntimeException> clazz )
            {
                try
                {
                    throw clazz.newInstance();
                }
                catch( Throwable t )
                {
                    if( clazz.isInstance( t ) )
                    {
                        throw clazz.cast( t );
                    }
                    // Ignore
                }
            }

            public void publicThrowException( Class<? extends RuntimeException> clazz )
            {
                try
                {
                    throw clazz.newInstance();
                }
                catch( Throwable t )
                {
                    if( clazz.isInstance( t ) )
                    {
                        throw clazz.cast( t );
                    }
                    // Ignore
                }
            }
        }
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( TestTransient.class );
    }

    @Test( expected = TestException.class )
    public void invokeCustomExceptionThrowingMethod()
        throws Throwable
    {
        this.module.newTransient( TestTransient.class ).declaredMethodThatThrowsCustomException();
    }

    @Test( expected = TestException.class )
    public void invokeDeclaredPublicMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeDeclaredPublicMethodThatThrowsCustomException();
    }

    @Test( expected = TestException.class )
    public void invokeUndeclaredPublicMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeUndeclaredPublicMethodThatThrowsCustomException();
    }

    @Test( expected = TestException.class )
    public void invokePrivateMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        this.module.newTransient( TestTransient.class ).invokePrivateMethodThatThrowsCustomException();
    }

    @Test( expected = TestException.class )
    public void invokeProtectedMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeProtectedMethodThatThrowsCustomException();
    }

    @Test( expected = IllegalArgumentException.class )
    public void invokeBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .declaredMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = IllegalArgumentException.class )
    public void invokeDeclaredPublicMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = IllegalArgumentException.class )
    public void invokeUndeclaredPublicMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = IllegalArgumentException.class )
    public void invokePrivateMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokePrivateMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = IllegalArgumentException.class )
    public void invokeProtectedMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeProtectedMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = RuntimeException.class )
    public void invokeBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .declaredMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = RuntimeException.class )
    public void invokeDeclaredPublicMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = RuntimeException.class )
    public void invokeUndeclaredPublicMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = RuntimeException.class )
    public void invokePrivateMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokePrivateMethodThatThrowsBuiltinInheritedException();
    }

    @Test( expected = RuntimeException.class )
    public void invokeProtectedMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        this.module
            .newTransient( TestTransient.class )
            .invokeProtectedMethodThatThrowsBuiltinInheritedException();
    }
}
