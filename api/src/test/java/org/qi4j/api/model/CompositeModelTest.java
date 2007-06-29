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
package org.qi4j.api.model;

import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.api.Composite;
import org.qi4j.api.strategy.CompositeImpl;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Modifies;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

public class CompositeModelTest
{

    @Test( expected = NullArgumentException.class )
    public void constructorWithNullComposite()
    {
       new CompositeModel( null );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonInterfaceComposite()
    {
        new CompositeModel( ((Composite) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { Composite.class },
            new InvocationHandler() {
                public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
                {
                    return null;
                }
            })).getClass() );
    }

    @Test( expected = InvalidCompositeException.class )
    public void constructorWithNonComposite()
    {
        new CompositeModel( (Class) Serializable.class );
    }

    @Test
    public void getCompositeClass()
    {
        assertEquals( TestComposite.class, new CompositeModel( TestComposite.class ).getCompositeClass() );
    }

    @Test
    public void getImplementations()
    {
        CompositeModel model = new CompositeModel( TestComposite.class );
        Set<Class> expected = new HashSet<Class>( 3 );
        expected.add( TestComposite1.class );
        expected.add( TestComposite2.class );
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
                    new CompositeModel( ExtendedTestComposite.class ).getImplementations().size() > 0);
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterImplementationsFromOutside()
    {
        CompositeModel model = new CompositeModel( TestComposite.class );
        List<MixinModel> mixinsModels = model.getImplementations();
        int nrOfMixins = mixinsModels.size();
        mixinsModels.clear();
        mixinsModels = model.getImplementations();
        assertTrue( "mixins list should be unmodifiable", nrOfMixins == mixinsModels.size() );
    }

    @Test
    public void getModifiers( )
    {
        CompositeModel model = new CompositeModel( TestComposite.class );
        Set<Class> expected = new HashSet<Class>( 3 );
        expected.add( TestComposite1.class );
        expected.add( TestComposite2.class );
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
                    new CompositeModel( ExtendedTestComposite.class ).getImplementations().size() > 0);
    }

    // we should not be able to alter the mixins list from outside
    @Test
    public void alterModifiersFromOutside()
    {
        CompositeModel model = new CompositeModel( TestComposite.class );
        List<ModifierModel> modifierModels = model.getModifiers();
        int nrOfMixins = modifierModels.size();
        modifierModels.clear();
        modifierModels = model.getModifiers();
        assertTrue( "modifiers list should be unmodifiable", nrOfMixins == modifierModels.size() );
    }


    @ImplementedBy ( { TestComposite1.class, TestComposite2.class, TestComposite1.class } )
    @ModifiedBy( { TestComposite1.class, TestComposite2.class, TestComposite1.class } )
    private interface TestComposite extends Composite
    {
        
    }

    private interface ExtendedTestComposite extends TestComposite
    {

    }

    private class TestComposite1
    {
        @Modifies InvocationHandler handler;
    }

    private class TestComposite2
    {
        @Modifies InvocationHandler handler;
    }
}
