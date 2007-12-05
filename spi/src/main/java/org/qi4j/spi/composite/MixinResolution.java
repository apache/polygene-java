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
package org.qi4j.spi.composite;

/**
 * A mixin is an implementation of a particular interface,
 * and is used as a fragment in a composite.
 */
public final class MixinResolution
    extends FragmentResolution
{
    // Constructors --------------------------------------------------
    public MixinResolution( MixinModel mixinModel, Iterable<ConstructorResolution> constructorResolutions, Iterable<FieldResolution> fieldResolutions, Iterable<MethodResolution> methodResolutions )
    {
        super( mixinModel, constructorResolutions, fieldResolutions, methodResolutions );
    }

    public MixinModel getMixinModel()
    {
        return (MixinModel) getAbstractModel();
    }
}