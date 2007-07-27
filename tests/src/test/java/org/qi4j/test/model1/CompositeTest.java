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
import java.util.List;
import junit.framework.TestCase;
import org.qi4j.runtime.CompositeModelImpl;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.InvalidModifierException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.runtime.CompositeImpl;
import org.qi4j.runtime.CompositeModelFactoryImpl;

public class CompositeTest extends TestCase
{
    private CompositeModelFactoryImpl modelFactory;

    protected void setUp() throws Exception
    {
        modelFactory = new CompositeModelFactoryImpl();
    }

    public void testComposition1()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.getCompositeModel( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        List<MixinModel> lists = composite1.getImplementations();
        assertEquals( 2, lists.size() );
        MixinModel mixinModel = lists.get( 1 );
        assertEquals( Mixin1Impl.class, mixinModel.getFragmentClass() );
        mixinModel = lists.get( 0 );
        assertEquals( CompositeImpl.class, mixinModel.getFragmentClass() );
        List<ModifierModel> modifiers1 = composite1.getModifiers();
        assertEquals( 2, modifiers1.size() );
        ModifierModel modifierModel = modifiers1.get( 1 );
        assertEquals( Modifier1.class, modifierModel.getFragmentClass() );

        List<MixinModel> mixinModifiers = new ArrayList<MixinModel>();
        mixinModifiers.add( lists.get( 1 ) );
        assertEquals( mixinModifiers, composite1.getImplementations( Mixin1.class ) );
        CompositeModel composite2 =modelFactory.getCompositeModel( Composition1.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<ModifierModel> modifiers2 = lists.get( 1 ).getModifiers();
        assertEquals( 1, modifiers2.size() );
        ModifierModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesField() );
    }

    public void testComposition2()
        throws Exception
    {
        CompositeModel composite1 = modelFactory.getCompositeModel( Composition2.class );
        assertEquals( Composition2.class, composite1.getCompositeClass() );
        List<MixinModel> lists = composite1.getImplementations();
        assertEquals( 3, lists.size() );

        MixinModel mixin1 = lists.get( 1 );
        assertEquals( Mixin1Impl.class, mixin1.getFragmentClass() );
        MixinModel mixin2 = lists.get( 2 );
        assertEquals( Mixin2Impl.class, mixin2.getFragmentClass() );
        List<ModifierModel> modifiers1 = composite1.getModifiers();
        assertEquals( 2, modifiers1.size() );
        assertEquals( lists.get( 1 ), composite1.getImplementations( Mixin1.class ).get( 0 ) );
        CompositeModel composite2 =modelFactory.getCompositeModel( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<ModifierModel> modifiers3 = mixin2.getModifiers();
        assertEquals( 0, modifiers3.size() );

        List<ModifierModel> modifiers2 = mixin1.getModifiers();
        assertEquals( 1, modifiers2.size() );
        ModifierModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesField() );
    }

    // Testing that system check that at least one @Modifies field exist.
    public void testComposition3()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.getCompositeModel( Composition3.class );
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
            CompositeModel composite =modelFactory.getCompositeModel( Composition4.class );
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
            CompositeModel composite = modelFactory.getCompositeModel( (Class) null );
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
            CompositeModel composite =modelFactory.getCompositeModel( Composition5.class );
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
            CompositeModel composite = modelFactory.getCompositeModel( Composition7.class );
            assertNotNull( composite );
            fail( "Should throw InvalidCompositeException." );
        }
        catch( InvalidCompositeException e )
        {
            //Expected
        }

    }
}
