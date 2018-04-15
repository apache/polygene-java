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

import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JAVADOC
 */
public class QI256Test
    extends AbstractPolygeneTest
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
    public interface TestTransient
        extends TransientComposite
    {
        void declaredMethodThatThrowsCustomException();

        void invokeDeclaredPublicMethodThatThrowsCustomException();

        void invokePrivateMethodThatThrowsCustomException();

        void invokeProtectedMethodThatThrowsCustomException();

        void invokeUndeclaredPublicMethodThatThrowsCustomException();

        void declaredMethodThatThrowsBuiltinInheritedException();

        void invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();

        void invokePrivateMethodThatThrowsBuiltinInheritedException();

        void invokeProtectedMethodThatThrowsBuiltinInheritedException();

        void invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();

        void declaredMethodThatThrowsBuiltinException();

        void invokeDeclaredPublicMethodThatThrowsBuiltinException();

        void invokePrivateMethodThatThrowsBuiltinException();

        void invokeProtectedMethodThatThrowsBuiltinException();

        void invokeUndeclaredPublicMethodThatThrowsBuiltinException();

        abstract class TestTransientMixin
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

    @Test
    public void invokeCustomExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( TestException.class, () -> {
            this.transientBuilderFactory.newTransient( TestTransient.class ).declaredMethodThatThrowsCustomException();
        } );
    }

    @Test
    public void invokeDeclaredPublicMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( TestException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeDeclaredPublicMethodThatThrowsCustomException();
        } );
    }

    @Test
    public void invokeUndeclaredPublicMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( TestException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeUndeclaredPublicMethodThatThrowsCustomException();
        } );
    }

    @Test
    public void invokePrivateMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( TestException.class, () -> {
            this.transientBuilderFactory.newTransient( TestTransient.class )
                .invokePrivateMethodThatThrowsCustomException();
        } );
    }

    @Test
    public void invokeProtectedMethodWhichInvokesCustomExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( TestException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeProtectedMethodThatThrowsCustomException();
        } );
    }

    @Test
    public void invokeBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( IllegalArgumentException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .declaredMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeDeclaredPublicMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( IllegalArgumentException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeUndeclaredPublicMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( IllegalArgumentException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokePrivateMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( IllegalArgumentException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokePrivateMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeProtectedMethodWhichInvokesBuiltinInheritedExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( IllegalArgumentException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeProtectedMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( RuntimeException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .declaredMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeDeclaredPublicMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( RuntimeException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeDeclaredPublicMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeUndeclaredPublicMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( RuntimeException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeUndeclaredPublicMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokePrivateMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( RuntimeException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokePrivateMethodThatThrowsBuiltinInheritedException();
        } );
    }

    @Test
    public void invokeProtectedMethodWhichInvokesBuiltinExceptionThrowingMethod()
        throws Throwable
    {
        assertThrows( RuntimeException.class, () -> {
            this.transientBuilderFactory
                .newTransient( TestTransient.class )
                .invokeProtectedMethodThatThrowsBuiltinInheritedException();
        } );
    }
}
