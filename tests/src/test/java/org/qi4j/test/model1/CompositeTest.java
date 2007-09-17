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
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.InvalidModifierException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.NullArgumentException;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.api.Composite;
import org.qi4j.runtime.CompositeModelFactory;
import org.qi4j.runtime.MixinModelFactory;
import org.qi4j.runtime.ModifierModelFactory;
import org.qi4j.runtime.LifecycleImpl;
import org.qi4j.runtime.CompositeImpl;
import org.qi4j.runtime.CompositeServicesModifier;

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

        ModifierModelFactory modifierBuilder = new ModifierModelFactory();
        MixinModelFactory mmb = new MixinModelFactory( modifierBuilder );
        reference.add( mmb.getMixinModel( Mixin1Impl.class, Composition1.class ) );
        reference.add( mmb.getMixinModel( CompositeImpl.class, Composition1.class ) );
        reference.add( mmb.getMixinModel( LifecycleImpl.class, Composition1.class ) );

        CompositeModel composite1 = modelFactory.newCompositeModel( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        Iterable<MixinModel> modelMixins = composite1.getMixinModels();

        assertEquals( reference, modelMixins );

        List<ModifierModel> referenceModifiers = new ArrayList<ModifierModel>();
        referenceModifiers.add( modifierBuilder.newModifierModel( Modifier1.class, Composition1.class ) );
        referenceModifiers.add( modifierBuilder.newModifierModel( CompositeServicesModifier.class, Composition1.class ) );

        assertEquals( referenceModifiers, composite1.getModifierModels() );
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
        List<ModifierModel> modifiers1 = composite1.getModifierModels();
        assertEquals( 2, modifiers1.size() );
        assertEquals( lists.get( 1 ), composite1.getImplementations( Mixin1.class ).get( 0 ) );
        CompositeModel composite2 =modelFactory.newCompositeModel( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<ModifierModel> modifiers3 = mixin2.getModifiers();
        assertEquals( 0, modifiers3.size() );

        List<ModifierModel> modifiers2 = mixin1.getModifiers();
        assertEquals( 1, modifiers2.size() );
        ModifierModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesDependency() );
*/
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
