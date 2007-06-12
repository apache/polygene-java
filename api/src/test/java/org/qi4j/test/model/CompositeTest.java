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
package org.qi4j.test.model;

import java.util.List;
import junit.framework.TestCase;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.IllegalModifierException;
import org.qi4j.api.model.MissingModifiesFieldException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.MultipleModifiesFieldException;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.Composite;
import org.qi4j.api.strategy.CompositeImpl;

public class CompositeTest extends TestCase
{
    public void testComposition1()
        throws Exception
    {
        CompositeModel composite1 = new CompositeModel( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        List<MixinModel> lists = composite1.getImplementations();
        assertEquals( 2, lists.size() );
        MixinModel mixinModel = lists.get( 0 );
        assertEquals( Mixin1Impl.class, mixinModel.getFragmentClass() );
        mixinModel = lists.get( 1 );
        assertEquals( CompositeImpl.class, mixinModel.getFragmentClass() );
        List<ModifierModel> modifiers1 = composite1.getModifiers();
        assertEquals( 1, modifiers1.size() );
        assertEquals( lists, composite1.getImplementations( Mixin1.class ) );
        CompositeModel composite2 = new CompositeModel( Composition1.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<ModifierModel> modifiers2 = mixinModel.getModifiers();
        assertEquals( 1, modifiers2.size() );
        ModifierModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesField() );
    }

    public void testComposition2()
        throws Exception
    {
        CompositeModel composite1 = new CompositeModel( Composition2.class );
        assertEquals( Composition2.class, composite1.getCompositeClass() );
        List<MixinModel> lists = composite1.getImplementations();
        assertEquals( 2, lists.size() );
        
        MixinModel mixin1 = lists.get( 0 );
        assertEquals( Mixin1Impl.class, mixin1.getFragmentClass() );
        MixinModel mixin2 = lists.get( 1 );
        assertEquals( Mixin2Impl.class, mixin2.getFragmentClass() );
        List<ModifierModel> modifiers1 = composite1.getModifiers();
        assertEquals( 1, modifiers1.size() );
        assertEquals( lists.get(0), composite1.getImplementations( Mixin1.class ).get(0) );
        CompositeModel composite2 = new CompositeModel( Composition2.class );
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
            CompositeModel composite1 = new CompositeModel( Composition3.class );
            fail( "Should throw an MissingModifiesFieldException.");
        } catch( MissingModifiesFieldException e )
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
            CompositeModel composite1 = new CompositeModel( Composition4.class );
            fail( "Should throw an IllegalModifierException.");
        } catch( IllegalModifierException e )
        {
            // Expected
        }
    }

    public void testCompositionNull()
        throws Exception
    {
        try
        {
            CompositeModel composite1 = new CompositeModel( null );
            fail( "Should throw an NullArgumentException.");
        } catch( NullArgumentException e )
        {
            // Expected
        }
    }
    
    public void testCompositeMultipleNexts()
        throws Exception
    {        
        try
        {
            CompositeModel composite5 = new CompositeModel(Composition5.class);
            
            fail( "Should throw MultipleModifiesFieldException." );
        }
        catch ( MultipleModifiesFieldException e )
        {
            //Expected
        }
    }
    
}
