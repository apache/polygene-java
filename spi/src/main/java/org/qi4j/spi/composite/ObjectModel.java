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

import java.util.List;
import org.qi4j.spi.property.PropertyModel;

/**
 * Model for regular object classes.
 */
public final class ObjectModel extends AbstractModel
{
    private final Iterable<ObjectMethodModel> objectMethodModels;
    private final Iterable<PropertyModel> propertyModels;
    private final Iterable<AssociationModel> associationModels;

    public ObjectModel( Class modelClass, Iterable<ConstructorModel> constructorModels, Iterable<FieldModel> fieldModels, Iterable<MethodModel> methodModels, Iterable<ObjectMethodModel> objectMethodModels, Iterable<PropertyModel> propertyModels, List<AssociationModel> associationModels )
    {
        super( modelClass, constructorModels, fieldModels, methodModels );
        this.objectMethodModels = objectMethodModels;
        this.propertyModels = propertyModels;
        this.associationModels = associationModels;
    }

    public Iterable<ObjectMethodModel> getObjectMethodModels()
    {
        return objectMethodModels;
    }

    public Iterable<PropertyModel> getPropertyModels()
    {
        return propertyModels;
    }

    public Iterable<AssociationModel> getAssociationModels()
    {
        return associationModels;
    }
}