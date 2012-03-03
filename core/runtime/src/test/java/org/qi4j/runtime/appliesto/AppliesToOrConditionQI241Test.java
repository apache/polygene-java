package org.qi4j.runtime.appliesto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.junit.Test;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.InjectionScope;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertTrue;

public class AppliesToOrConditionQI241Test
    extends AbstractQi4jTest
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
        UnitOfWork uow = module.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithFirstAnnotation> refWithFirst = module.findService(
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
        UnitOfWork uow = module.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithSecondAnnotation> refWithSecond = module.findService(
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
        UnitOfWork uow = module.newUnitOfWork();

        try
        {
            ServiceReference<SomeServiceCompositeWithTwoAnnotations> refWithTwo = module.findService(
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
