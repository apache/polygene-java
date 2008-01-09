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

import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.Visibility;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see MixinModel
 * @see ConcernModel
 */
public final class ObjectModel extends AbstractModel
{
    private final Iterable<PropertyModel> propertyModels;

    public ObjectModel( Class modelClass, Iterable<ConstructorModel> constructorModels, Iterable<FieldModel> fieldModels, Iterable<MethodModel> methodModels, Iterable<PropertyModel> propertyModels )
    {
        super( modelClass, constructorModels, fieldModels, methodModels );
        this.propertyModels = propertyModels;
    }

    public Iterable<PropertyModel> getPropertyModels()
    {
        return propertyModels;
    }
}