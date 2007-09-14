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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

public class CompositeModelTest
{
    private MixinModelBuilder mixinModelBuilder;
    private CompositeModelBuilder compositeModelBuilder;
    private ModifierModelBuilder modifierModelBuilder;


    public CompositeModelTest()
    {
        modifierModelBuilder = new ModifierModelBuilder();
        mixinModelBuilder = new MixinModelBuilder( modifierModelBuilder );
        compositeModelBuilder = new CompositeModelBuilder( modifierModelBuilder, mixinModelBuilder );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonInterfaceComposite()
    {
        compositeModelBuilder.getCompositeModel( Composite.class );
    }

    @Test
    public void getCompositeClass()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( TestComposite.class );
        Class compositeClass = model.getCompositeClass();
        assertEquals( TestComposite.class, compositeClass );
    }

    @Test
    public void getImplementations()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( TestMixin1.class );
        expected.add( TestMixin2.class );
        expected.add( TestMixin1.class );
        expected.add( CompositeImpl.class ); // from Composite itself
        Iterable<MixinModel> list = model.getMixinModels();
        for( MixinModel mixinModel : list )
        {
            assertTrue( "unexpected mixin model: " + mixinModel, expected.remove( mixinModel.getFragmentClass() ) );
        }
        assertTrue( "unexpected mixin modles: ", expected.size() == 0 );
    }

    @Test
    public void getImplementationsForSubclasses()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( ExtendedTestComposite.class );
        Iterable mixinModels = model.getMixinModels();
        assertTrue( "subclasses should inherit mixins", mixinModels.iterator().hasNext() );
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterImplementationsFromOutside()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( TestComposite.class );
        Iterable<MixinModel> mixinsModels = model.getMixinModels();
        try
        {
            mixinsModels.iterator().remove();
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
        CompositeModel model = compositeModelBuilder.getCompositeModel( TestComposite.class );
        List<Class> expected = new LinkedList<Class>();
        expected.add( TestModifier1.class );
        expected.add( TestModifier2.class );
        expected.add( TestModifier1.class );
        expected.add( CompositeServicesModifier.class );
        Iterable<ModifierModel> list = model.getModifierModels();
        for( ModifierModel modifierModel : list )
        {
            assertTrue( "unexpected modifier model: " + modifierModel, expected.remove( modifierModel.getFragmentClass() ) );
        }
        assertTrue( "unexpected modifier modles: ", expected.size() == 0 );
    }

    @Test
    public void getModifiersForSubclasses()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( ExtendedTestComposite.class );
        Iterable modifierModels = model.getModifierModels();
        assertTrue( "subclasses should inherit modifiers", modifierModels.iterator().hasNext() );
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterModifiersFromOutside()
    {
        CompositeModel model = compositeModelBuilder.getCompositeModel( TestComposite.class );
        Iterable<ModifierModel> modifierModels = model.getModifierModels();
        try
        {
            modifierModels.iterator().remove();
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

    private static class TestInvocationHandler implements InvocationHandler
    {
        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            return null;
        }
    }
}
