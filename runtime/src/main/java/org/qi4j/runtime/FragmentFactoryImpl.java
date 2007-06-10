/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.runtime;

import org.qi4j.api.FragmentFactory;
import org.qi4j.api.ObjectInstantiationException;
import org.qi4j.api.model.CompositeInterface;
import org.qi4j.api.model.Fragment;

/**
 * Default fragment factory. Simply instantiates the fragment class using newInstance.
 */
public final class FragmentFactoryImpl
    implements FragmentFactory
{
    public Object newFragment( Fragment aFragment, CompositeInterface aCompositeInterface )
    {
        try
        {
            return aFragment.getFragmentClass().newInstance();
        }
        catch( Exception e )
        {
            throw new ObjectInstantiationException( "Could not instantiate class " + aFragment.getFragmentClass().getName(), e );
        }
    }
}
