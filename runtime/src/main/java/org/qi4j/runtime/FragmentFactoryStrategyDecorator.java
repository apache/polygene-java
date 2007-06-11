/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.runtime;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.model.CompositeInterface;
import org.qi4j.api.model.Fragment;
import org.qi4j.api.model.Mixin;
import org.qi4j.api.model.Modifier;

/**
 * FragmentFactory decorator that can map one class to another. This
 * makes it possible, for example, to specify an interface as a modifier
 * and then map that to a concrete class here.
 *
 */
public class FragmentFactoryStrategyDecorator
    implements FragmentFactory
{
    // Static --------------------------------------------------------

    // Attributes ----------------------------------------------------
    FragmentFactory delegate;
    Map<Class, Class> mappings = new HashMap<Class, Class>();

    // Constructors --------------------------------------------------
    public FragmentFactoryStrategyDecorator( FragmentFactory delegate )
    {
        this.delegate = delegate;
    }

    // Public --------------------------------------------------------
    public void addMapping( Class aFrom, Class aTo )
    {
        mappings.put( aFrom, aTo );
    }

    // FragmentFactory implementation --------------------------------
    public Object newFragment( Fragment aFragment, CompositeInterface aCompositeInterface ) throws ObjectInstantiationException
    {
        Class to = mappings.get( aFragment.getFragmentClass() );

        if( to != null )
        {
            if( aFragment instanceof Modifier )
            {
                return delegate.newFragment( new Modifier( to ), aCompositeInterface );
            }
            else
            {
                return delegate.newFragment( new Mixin( to ), aCompositeInterface );
            }
        }
        else
        {
            return delegate.newFragment( aFragment, aCompositeInterface );
        }
    }
}
