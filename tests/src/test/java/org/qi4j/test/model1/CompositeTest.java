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
import org.qi4j.api.model.AssertionModel;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.InvalidFragmentException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.runtime.AssertionModelFactory;
import org.qi4j.runtime.CompositeMixin;
import org.qi4j.runtime.CompositeModelFactory;
import org.qi4j.runtime.MixinModelFactory;
import org.qi4j.runtime.SideEffectModelFactory;

public class CompositeTest extends TestCase
{
    private CompositeModelFactory modelFactory;

    protected void setUp() throws Exception
    {
        modelFactory = new CompositeModelFactory();
    }

    public void testComposition1()
        throws Exception
    {
        List<MixinModel> reference = new ArrayList<MixinModel>();

        AssertionModelFactory assertionBuilder = new AssertionModelFactory();
        SideEffectModelFactory sideEffectBuilder = new SideEffectModelFactory();
        MixinModelFactory mmb = new MixinModelFactory( assertionBuilder, sideEffectBuilder );
        reference.add( mmb.newFragmentModel( Mixin1Impl.class, Composition1.class, Composition1.class ) );
        reference.add( mmb.newFragmentModel( CompositeMixin.class, Composition1.class, Composition1.class ) );

        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        Iterable<MixinModel> modelMixins = composite1.getMixinModels();

        assertEquals( reference, modelMixins );

        List<AssertionModel> referenceAssertions = new ArrayList<AssertionModel>();
        referenceAssertions.add( assertionBuilder.newFragmentModel( Modifier1.class, Composition1.class, Composition1.class ) );

        assertEquals( referenceAssertions, composite1.getAssertionModels() );
    }

    public void testComposition2()
        throws Exception
    {
/*
        CompositeModel composite1 = modelFactory.newCompositeModel( Composition2.class );
        assertEquals( Composition2.class, composite1.getCompositeClass() );
        List<MixinModel> lists = composite1.getMixinModels();
        assertEquals( 3, lists.size() );

        MixinModel mixin1 = lists.get( 1 );
        assertEquals( Mixin1Impl.class, mixin1.getFragmentClass() );
        MixinModel mixin2 = lists.get( 2 );
        assertEquals( Mixin2Impl.class, mixin2.getFragmentClass() );
        List<AssertionModel> modifiers1 = composite1.getAssertionModels();
        assertEquals( 2, modifiers1.size() );
        assertEquals( lists.get( 1 ), composite1.getImplementations( Mixin1.class ).get( 0 ) );
        CompositeModel composite2 =modelFactory.newCompositeModel( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<AssertionModel> modifiers3 = mixin2.getModifiers();
        assertEquals( 0, modifiers3.size() );

        List<AssertionModel> modifiers2 = mixin1.getModifiers();
        assertEquals( 1, modifiers2.size() );
        AssertionModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getSideEffectForDependency() );
*/
    }

    // Testing that system check that at least one @AssertionFor field exist.
    public void testComposition3()
        throws Exception
    {
        try
        {
            CompositeModel composite = modelFactory.newCompositeModel( Composition3.class );
            fail( "Should throw an InvalidFragmentException." );
        }
        catch( InvalidFragmentException e )
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
            fail( "Should throw an InvalidFragmentException." );
        }
        catch( InvalidFragmentException e )
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
            fail( "Should throw InvalidFragmentException." );
        }
        catch( InvalidFragmentException e )
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
