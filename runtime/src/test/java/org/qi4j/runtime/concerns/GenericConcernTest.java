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

package org.qi4j.runtime.concerns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.Test;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Tests for GenericConcern
 */
public class GenericConcernTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addTransients( SomeComposite.class );
    }

    @Test
    public void testNestedUnitOfWork()
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        Some some = transientBuilderFactory.newTransient( Some.class );
        some.doStuff();
        uow.discard();
    }

    @Concerns( NestedUnitOfWorkConcern.class )
    @Mixins( SomeMixin.class )
    public interface SomeComposite
        extends Some, TransientComposite
    {
    }

    public interface Some
    {
        @NestedUnitOfWork
        public String doStuff();
    }

    public static abstract class SomeMixin
        implements Some
    {
        public String doStuff()
        {
            return "Blah blah";
        }
    }

    @AppliesTo( NestedUnitOfWork.class )
    public static class NestedUnitOfWorkConcern
        extends GenericConcern
    {
        @Structure
        UnitOfWorkFactory uowf;

        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            UnitOfWork nested = uowf.nestedUnitOfWork();

            try
            {
                Object value = next.invoke( o, method, objects );
                nested.complete();
                return value;
            }
            catch( Throwable throwable )
            {
                nested.discard();
                throw throwable;
            }
        }
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    public @interface NestedUnitOfWork
    {
    }
}