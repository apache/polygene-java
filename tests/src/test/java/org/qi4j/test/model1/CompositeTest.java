/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.model1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.InvalidModifierException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.runtime.CompositeImpl;
import org.qi4j.runtime.CompositeModelBuilder;
import org.qi4j.runtime.CompositeModelFactoryImpl;
import org.qi4j.runtime.MixinModelBuilder;
import org.qi4j.runtime.ModifierModelBuilder;

public class CompositeTest extends TestCase
{
    private CompositeModelFactoryImpl modelFactory;
    private CompositeModelBuilder compositeModelBuilder;
    private MixinModelBuilder mixinModelBuilder;
    private ModifierModelBuilder modifierModelBuilder;

    protected void setUp() throws Exception
    {
        modifierModelBuilder = new ModifierModelBuilder();
        mixinModelBuilder = new MixinModelBuilder( modifierModelBuilder );
        compositeModelBuilder = new CompositeModelBuilder( modifierModelBuilder, mixinModelBuilder );
        modelFactory = new CompositeModelFactoryImpl( compositeModelBuilder );
    }

    public void testComposition1Mixins()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        Iterable<MixinModel> lists = composite1.getMixinModels();
        Iterator<MixinModel> check = lists.iterator();
        MixinModel mixinModel = check.next();
        assertEquals( CompositeImpl.class, mixinModel.getFragmentClass() );
        mixinModel = check.next();
        assertEquals( Mixin1Impl.class, mixinModel.getFragmentClass() );
    }

    public void testComposition1Modifiers()
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        Iterable<ModifierModel> list = composite1.getModifierModels();
        Iterator<ModifierModel> modifiers1 = list.iterator();
        ModifierModel modifierModel1 = modifiers1.next();

        ModifierModel modifierModel2 = modifiers1.next();
        assertEquals( Modifier1.class, modifierModel2.getFragmentClass() );
    }

    public void testComposition1ImplMixins()
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        Iterable<MixinModel> lists = composite1.getMixinModels();
        Iterator<MixinModel> mixins = lists.iterator();
        mixins.next();
        List<MixinModel> mixinModifiers = new ArrayList<MixinModel>();
        mixinModifiers.add( mixins.next() );
        assertEquals( mixinModifiers, composite1.getImplementations( Mixin1.class ) );
        CompositeModel composite2 = modelFactory.newCompositeModel( Composition1.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );
    }

    public void testComposition1ImplModifiers()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        Iterable<MixinModel> lists = composite1.getMixinModels();
        Iterator<MixinModel> mixins = lists.iterator();
        mixins.next();
        Iterable<ModifierModel> modifierList = mixins.next().getModifiers();
        Iterator<ModifierModel> modifiers = modifierList.iterator();
        ModifierModel modifier4 = modifiers.next();
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesDependency() );
    }

    public void testComposition2a()
        throws Exception
    {
        CompositeModel composite = modelFactory.newCompositeModel( Composition2.class );
        assertEquals( Composition2.class, composite.getCompositeClass() );
        Iterable<MixinModel> lists = composite.getMixinModels();
        Iterator<MixinModel> mixinModels = lists.iterator();
        mixinModels.next(); // skip 1
        MixinModel mixin1 = mixinModels.next();
        assertEquals( Mixin1Impl.class, mixin1.getFragmentClass() );
        MixinModel mixin2 = mixinModels.next();
        assertEquals( Mixin2Impl.class, mixin2.getFragmentClass() );
    }

    public void testComposition2b()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition2.class );
        Iterable<ModifierModel> modifiers = composite1.getModifierModels();
        Iterator<ModifierModel> modifierModels = modifiers.iterator();
        modifierModels.next(); // skip 1
        ModifierModel modifier1 = modifierModels.next();
        assertEquals( modifier1, composite1.getImplementations( Mixin1.class ).get( 0 ) );
        CompositeModel composite2 = modelFactory.newCompositeModel( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );
    }

    public void testComposition2c()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition2.class );
        Iterable<MixinModel> mixinList = composite1.getMixinModels();
        Iterator<MixinModel> mixinModels = mixinList.iterator();
        mixinModels.next();
        MixinModel mixinModel1 = mixinModels.next();
        MixinModel mixinModel2 = mixinModels.next(); // Third one.

        Iterable<ModifierModel> modifiers1 = mixinModel1.getModifiers();
        Iterator<ModifierModel> modifierModels1 = modifiers1.iterator();

        Iterable<ModifierModel> modifiers2 = mixinModel2.getModifiers();
        Iterator<ModifierModel> modifierModels2 = modifiers2.iterator();

        assertFalse( modifierModels1.hasNext() );
        assertTrue( modifierModels2.hasNext() );

        ModifierModel modifier4 = modifierModels2.next();
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesDependency() );
    }

    // Testing that system check that at least one @Modifies field exist.
    public void testComposition3()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( Composition3.class );
            fail( "Should throw an InvalidModifierException." );
        }
        catch( InvalidModifierException e )
        {
            // Expected
        }
    }

    // Test that interfaces can't be modifiers.
    public void testComposition4()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( Composition4.class );
            fail( "Should throw an InvalidModifierException." );
        }
        catch( InvalidModifierException e )
        {
            // Expected
        }
    }

    public void testCompositionNull()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( (Class) null );
            fail( "Should throw an NullArgumentException." );
        }
        catch( NullArgumentException e )
        {
            // Expected
        }
    }

    public void testCompositeMultipleNexts()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( Composition5.class );
            fail( "Should throw InvalidModifierException." );
        }
        catch( InvalidModifierException e )
        {
            //Expected
        }
    }

    public void testCompositeIsNotInterface()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( Composition7.class );
            assertNotNull( composite );
            fail( "Should throw InvalidCompositeException." );
        }
        catch( InvalidCompositeException e )
        {
            //Expected
        }

    }
}
