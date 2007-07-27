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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;

public class CompositeModelTest
{

    @Test( expected = NullArgumentException.class )
    public void constructorWithNullComposite()
    {
        new CompositeModelImpl( null );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonInterfaceComposite()
    {
        new CompositeModelImpl( ( (Composite) Proxy.newProxyInstance(
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
        new CompositeModelImpl( (Class) Serializable.class );
    }

    @Test
    public void getCompositeClass()
    {
        assertEquals( TestComposite.class, new CompositeModelImpl( TestComposite.class ).getCompositeClass() );
    }

    @Test
    public void getImplementations()
    {
        CompositeModel model = new CompositeModelImpl( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( TestMixin1.class );
        expected.add( TestMixin2.class );
        expected.add( TestMixin1.class );
        expected.add( CompositeImpl.class ); // from Composite itself
        for( MixinModel mixinModel : model.getImplementations() )
        {
            assertTrue( "unexpected mixin model: " + mixinModel, expected.remove( mixinModel.getFragmentClass() ) );
        }
        assertTrue( "unexpected mixin modles: ", expected.size() == 0 );
    }

    @Test
    public void getImplementationsForSubclasses()
    {
        assertTrue( "subclasses should inherit mixins",
                    new CompositeModelImpl( ExtendedTestComposite.class ).getImplementations().size() > 0 );
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterImplementationsFromOutside()
    {
        CompositeModel model = new CompositeModelImpl( TestComposite.class );
        List<MixinModel> mixinsModels = model.getImplementations();
        int nrOfMixins = mixinsModels.size();
        try
        {
            mixinsModels.clear();
            fail( "mixins list should be unmodifiable" );
        }
        catch( UnsupportedOperationException e )
        {
            // Expected !!
        }
    }

    @Test
    public void getModifiers()
    {
        CompositeModel model = new CompositeModelImpl( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( TestModifier1.class );
        expected.add( TestModifier2.class );
        expected.add( TestModifier1.class );
        expected.add( CompositeServicesModifier.class );
        for( ModifierModel modifierModel : model.getModifiers() )
        {
            assertTrue( "unexpected modifier model: " + modifierModel, expected.remove( modifierModel.getFragmentClass() ) );
        }
        assertTrue( "unexpected modifier modles: ", expected.size() == 0 );
    }

    @Test
    public void getModifiersForSubclasses()
    {
        assertTrue( "subclasses should inherit modifiers",
                    new CompositeModelImpl( ExtendedTestComposite.class ).getImplementations().size() > 0 );
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterModifiersFromOutside()
    {
        CompositeModel model = new CompositeModelImpl( TestComposite.class );
        List<ModifierModel> modifierModels = model.getModifiers();
        int nrOfMixins = modifierModels.size();
        try
        {
            modifierModels.clear();
            fail( "modifiers list should be unmodifiable" );
        }
        catch( UnsupportedOperationException e )
        {
            // Expected!!
        }
    }


    @ImplementedBy( { TestMixin1.class, TestMixin2.class, TestMixin1.class } )
    @ModifiedBy( { TestModifier1.class, TestModifier2.class, TestModifier1.class } )
    private interface TestComposite extends Composite
    {

    }

    private interface ExtendedTestComposite extends TestComposite
    {

    }

    private class TestModifier1
    {
        @Modifies InvocationHandler handler;
    }

    private class TestModifier2
    {
        @Modifies InvocationHandler handler;
    }

    private class TestMixin1
    {
    }

    private class TestMixin2
    {
    }
}
