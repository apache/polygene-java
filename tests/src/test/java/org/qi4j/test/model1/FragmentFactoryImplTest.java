/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
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
package org.qi4j.test.model1;

import junit.framework.TestCase;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.runtime.FragmentFactoryImpl;

public class FragmentFactoryImplTest extends TestCase
{
    FragmentFactory fragmentFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        fragmentFactory = new FragmentFactoryImpl();
    }

    public void testNewFragmentForMixinModel() throws Exception
    {
        try
        {
            Object mixin = fragmentFactory.newFragment( new MixinModel( Mixin3.class ), null );
            fail( "FragmentFactoryImpl should not be able to instantiate a Mixin : " + Mixin3.class.getName() );
        }
        catch( CompositeInstantiationException e )
        {
            // Correct
        }
    }

    public void testNewFragmentForModifierModel() throws Exception
    {
        try
        {
            Object modifier = fragmentFactory.newFragment( new ModifierModel( Modifier7.class ), null );
            assertTrue( modifier instanceof Modifier7 );
        }
        catch( CompositeInstantiationException e )
        {
            fail( "FragmentFactoryImpl must be able to instantiate a Modifier : " + Modifier7.class.getName() );
        }
    }

}

