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
package org.apache.polygene.runtime.appliesto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Test;
import org.apache.polygene.api.common.AppliesTo;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.concern.ConcernOf;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.injection.InjectionScope;
import org.apache.polygene.api.injection.scope.Invocation;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

import static org.junit.Assert.assertTrue;

public class AppliesToOrConditionQI241Test
    extends AbstractPolygeneTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SomeServiceCompositeWithTwoAnnotations.class );
        module.services( SomeServiceCompositeWithFirstAnnotation.class );
        module.services( SomeServiceCompositeWithSecondAnnotation.class );
    }

    @Test
    public void testMultiConcerns1()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithFirstAnnotation> refWithFirst = serviceFinder.findService(
                SomeServiceCompositeWithFirstAnnotation.class );
            SomeServiceCompositeWithFirstAnnotation someWithFirst = refWithFirst.get();
            someWithFirst.doStuff();
            assertTrue( "AppliesTo did not match with first annotation", someWithFirst.concernHasBeenPlayed() );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void testMultiConcerns2()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithSecondAnnotation> refWithSecond = serviceFinder.findService(
                SomeServiceCompositeWithSecondAnnotation.class );
            SomeServiceCompositeWithSecondAnnotation someWithSecond = refWithSecond.get();
            someWithSecond.doStuff();
            assertTrue( "AppliesTo did not match with second annotation", someWithSecond.concernHasBeenPlayed() );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void testMultiConcernsBoth()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithTwoAnnotations> refWithTwo = serviceFinder.findService(
                SomeServiceCompositeWithTwoAnnotations.class );
            SomeServiceCompositeWithTwoAnnotations someWithTwo = refWithTwo.get();
            someWithTwo.doStuff();
            assertTrue( "AppliesTo did not match with two annotations", someWithTwo.concernHasBeenPlayed() );
        }
        finally
        {
            uow.discard();
        }
    }

    @Mixins( SomeMixinWithTwoAnnotations.class )
    @Concerns( MultiConcerns.class )
    public interface SomeServiceCompositeWithTwoAnnotations
        extends SomeService, ServiceComposite
    {
    }

    @Mixins( SomeMixinWithFirstAnnotation.class )
    @Concerns( MultiConcerns.class )
    public interface SomeServiceCompositeWithFirstAnnotation
        extends SomeService, ServiceComposite
    {
    }

    @Mixins( SomeMixinWithSecondAnnotation.class )
    @Concerns( MultiConcerns.class )
    public interface SomeServiceCompositeWithSecondAnnotation
        extends SomeService, ServiceComposite
    {
    }

    public interface SomeService
    {

        String doStuff();

        /**
         * Only for assertion purpose.
         */
        void playConcern();

        /**
         * Only for assertion purpose.
         */
        boolean concernHasBeenPlayed();
    }

    public static abstract class SomeBaseMixin
        implements SomeService
    {
        private boolean played = false;

        public void playConcern()
        {
            played = true;
        }

        public boolean concernHasBeenPlayed()
        {
            return played;
        }
    }

    public static abstract class SomeMixinWithTwoAnnotations
        extends SomeBaseMixin
        implements SomeService
    {
        @FirstAnnotation( "first one" )
        @SecondAnnotation( "second one" )
        public String doStuff()
        {
            return "Blah blah";
        }
    }

    public static abstract class SomeMixinWithFirstAnnotation
        extends SomeBaseMixin
        implements SomeService
    {

        @FirstAnnotation( "first one" )
        public String doStuff()
        {
            return "Blah blah";
        }
    }

    public static abstract class SomeMixinWithSecondAnnotation
        extends SomeBaseMixin
        implements SomeService
    {
        @SecondAnnotation( "second one" )
        public String doStuff()
        {
            return "Blah blah";
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    @InjectionScope
    public @interface FirstAnnotation
    {
        String value();
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    @InjectionScope
    public @interface SecondAnnotation
    {
        String value();
    }

    @AppliesTo( { FirstAnnotation.class, SecondAnnotation.class } )
    public static class MultiConcerns
        extends ConcernOf<InvocationHandler>
        implements InvocationHandler
    {

        @Optional
        @Invocation
        private FirstAnnotation first;

        @Optional
        @Invocation
        private SecondAnnotation second;

        @This
        private SomeService someServiceComposite;

        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            if( first != null )
            {
                System.err.println( "FIRST IS HERE AND HAS VALUE: " + first.value() );
            }
            if( second != null )
            {
                System.err.println( "SECOND IS HERE AND HAS VALUE: " + second.value() );
            }
            someServiceComposite.playConcern();
            return next.invoke( proxy, method, args );
        }
    }
}
