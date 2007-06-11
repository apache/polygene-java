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
}
