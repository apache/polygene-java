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

import junit.framework.TestCase;
import org.qi4j.api.model.Composite;
import org.qi4j.api.model.Mixin;
import org.qi4j.api.model.Modifier;
import org.qi4j.api.model.MissingModifiesFieldException;
import org.qi4j.api.model.IllegalModifierException;
import org.qi4j.api.model.NullArgumentException;
import java.util.List;

public class CompositeTest extends TestCase
{
    public void testComposition1()
        throws Exception
    {
        Composite composite1 = new Composite( Composition1.class );
        assertEquals( Composition1.class, composite1.getCompositeClass() );
        List<Mixin> list = composite1.getImplementations();
        assertEquals( 1, list.size() );
        Mixin mixin = list.get( 0 );
        assertEquals( Mixin1Impl.class, mixin.getFragmentClass() );
        List<Modifier> modifiers1 = composite1.getModifiers();
        assertEquals( 1, modifiers1.size() );
        assertEquals( list, composite1.getImplementations( Mixin1.class ) );
        Composite composite2 = new Composite( Composition1.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<Modifier> modifiers2 = mixin.getModifiers();
        assertEquals( 1, modifiers2.size() );
        Modifier modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesField() );
    }

    public void testComposition2()
        throws Exception
    {
        Composite composite1 = new Composite( Composition2.class );
        assertEquals( Composition2.class, composite1.getCompositeClass() );
        List<Mixin> list = composite1.getImplementations();
        assertEquals( 2, list.size() );
        
        Mixin mixin1 = list.get( 0 );
        assertEquals( Mixin1Impl.class, mixin1.getFragmentClass() );
        Mixin mixin2 = list.get( 1 );
        assertEquals( Mixin2Impl.class, mixin2.getFragmentClass() );
        List<Modifier> modifiers1 = composite1.getModifiers();
        assertEquals( 1, modifiers1.size() );
        assertEquals( list.get(0), composite1.getImplementations( Mixin1.class ).get(0) );
        Composite composite2 = new Composite( Composition2.class );
        assertEquals( composite1, composite2 );
        assertEquals( composite1.hashCode(), composite2.hashCode() );

        List<Modifier> modifiers3 = mixin2.getModifiers();
        assertEquals( 0, modifiers3.size() );

        List<Modifier> modifiers2 = mixin1.getModifiers();
        assertEquals( 1, modifiers2.size() );
        Modifier modifier4 = modifiers2.get( 0 );
        assertEquals( Modifier4.class, modifier4.getFragmentClass() );
        assertEquals( Modifier4.class.getDeclaredField( "next" ), modifier4.getModifiesField() );
    }

    // Testing that system check that at least one @Modifies field exist.
    public void testComposition3()
        throws Exception
    {
        try
        {
            Composite composite1 = new Composite( Composition3.class );
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
            Composite composite1 = new Composite( Composition4.class );
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
            Composite composite1 = new Composite( null );
            fail( "Should throw an NullArgumentException.");
        } catch( NullArgumentException e )
        {
            // Expected
        }
    }
}
