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

import java.util.List;
import junit.framework.TestCase;
import org.qi4j.composite.Composite;
import org.qi4j.composite.NullArgumentException;
import org.qi4j.runtime.composite.CompositeModelFactory;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.InvalidFragmentException;

public class CompositeModelFactoryTest extends TestCase
{
    private CompositeModelFactory modelFactory;

    protected void setUp() throws Exception
    {
        modelFactory = new CompositeModelFactory();
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
        List<ConcernModel> modifiers1 = composite1.getConcernModels();
        assertEquals( 2, modifiers1.size() );
        assertEquals( lists.get( 1 ), composite1.getImplementations( Mixin1.class ).get( 0 ) );
        CompositeModel composite2 =modelFactory.newCompositeModel( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<ConcernModel> modifiers3 = mixin2.getModifiers();
        assertEquals( 0, modifiers3.size() );

        List<ConcernModel> modifiers2 = mixin1.getModifiers();
        assertEquals( 1, modifiers2.size() );
        ConcernModel modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getSideEffectForDependency() );
*/
    }

    // Testing that system check that at least one @ConcernFor field exist.
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
            Class error = List.class;

            CompositeModel composite = modelFactory.newCompositeModel( (Class<? extends Composite>) error );
            assertNotNull( composite );
            fail( "Should throw InvalidCompositeException." );
        }
        catch( InvalidCompositeException e )
        {
            //Expected
        }

    }
}
