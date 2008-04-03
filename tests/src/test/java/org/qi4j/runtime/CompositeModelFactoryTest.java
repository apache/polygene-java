/*
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.runtime;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.property.PropertyMixin;
import org.qi4j.runtime.composite.CompositeMixin;
import org.qi4j.runtime.composite.CompositeModelFactory;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ConcernModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MixinModel;

public class CompositeModelFactoryTest
{
    private CompositeModelFactory factory = new CompositeModelFactory();

    @Test( expected = NullArgumentException.class )
    public void constructorWithNullComposite()
    {
        factory.newCompositeModel( null );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonInterfaceComposite()
    {
        factory.newCompositeModel( ( (Composite) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[]{ Composite.class },
            new InvocationHandler()
            {
                public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
                {
                    return null;
                }
            } ) ).getClass() );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonComposite()
    {
        factory.newCompositeModel( (Class) Serializable.class );
    }

    @Test
    public void getCompositeClass()
    {
        assertEquals( TestComposite.class, factory.newCompositeModel( TestComposite.class ).getCompositeType() );
    }

    @Test
    public void getImplementations()
    {
        CompositeModel model = factory.newCompositeModel( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( PropertyMixin.class );
        expected.add( TestMixin1.class );
        expected.add( TestMixin2.class );
        expected.add( TestMixin1.class );
        expected.add( CompositeMixin.class ); // from Composite itself
        Iterable<MixinModel> list = model.getMixinModels();
        for( MixinModel mixinModel : list )
        {
            assertTrue( "unexpected mixin model: " + mixinModel, expected.remove( mixinModel.getModelClass() ) );
        }
        assertTrue( "unexpected mixin models: ", expected.size() == 0 );
    }

    @Test
    public void getImplementationsForSubclasses()
    {
        assertTrue( "subclasses should inherit mixins",
                    factory.newCompositeModel( ExtendedTestComposite.class ).getMixinModels().iterator().hasNext() );
    }

    @Test
    public void getModifiers()
    {
        CompositeModel model = factory.newCompositeModel( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( TestModifier1.class );
        expected.add( TestModifier2.class );
        Iterable<ConcernModel> list = model.getConcernModels();
        for( ConcernModel concernModel : list )
        {
            assertTrue( "unexpected modifier model: " + concernModel, expected.remove( concernModel.getModelClass() ) );
        }
        assertTrue( "unexpected modifier models: ", expected.size() == 0 );
    }

    @Test
    public void getModifiersForSubclasses()
    {
        assertTrue( "subclasses should inherit modifiers",
                    factory.newCompositeModel( ExtendedTestComposite.class ).getMixinModels().iterator().hasNext() );
    }

    @Mixins( { TestMixin1.class, TestMixin2.class, TestMixin1.class } )
    @Concerns( { TestModifier1.class, TestModifier2.class, TestModifier1.class } )
    private interface TestComposite extends Composite
    {

    }

    private interface ExtendedTestComposite extends TestComposite
    {

    }

    private class TestModifier1
    {
        @ConcernFor InvocationHandler handler;
    }

    private class TestModifier2
    {
        @ConcernFor InvocationHandler handler;
    }

    private class TestMixin1
    {
    }

    private class TestMixin2
    {
    }
}
