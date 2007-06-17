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
package org.qi4j.api;

import org.qi4j.api.model.CompositeObject;
import org.qi4j.api.model.FragmentModel;


/**
 * This factory is responsible for instantiating fragments. This is where
 * you would put DI integrations.
 *
 * TODO Split into MixinFactory and ModifierFactory, and add proxy to newMixin()
 */
public interface FragmentFactory
{
    /**
     * Instantiate a new fragment. It could be either a modifier or a mixin.
     * The composite for which this modifier is instantiated is provided as extra context.
     *
     * @param aFragmentModel           the mixin or modifier to be instantiated
     * @param aCompositeObject the composite for which the fragment will be used
     * @return a fragment instance
     * @throws CompositeInstantiationException if the fragment could not be instantiated for any reason
     * @see org.qi4j.api.model.ModifierModel
     * @see org.qi4j.api.model.MixinModel
     */
    Object newFragment( FragmentModel aFragmentModel, CompositeObject aCompositeObject )
        throws CompositeInstantiationException;
}
