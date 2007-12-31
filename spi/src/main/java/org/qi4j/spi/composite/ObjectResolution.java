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
 * Base class for object model resolutions. Resolutions are models resolved in a runtime environment
 */
public final class ObjectResolution extends AbstractResolution
{
    private Iterable<PropertyResolution> propertyResolutions;

    public ObjectResolution( AbstractModel abstractModel, Iterable<ConstructorResolution> constructorResolutions, Iterable<FieldResolution> fieldResolutions, Iterable<MethodResolution> methodResolutions, Iterable<PropertyResolution> propertyResolutions )
    {
        super( abstractModel, constructorResolutions, fieldResolutions, methodResolutions );
        this.propertyResolutions = propertyResolutions;
    }

    public ObjectModel getObjectModel()
    {
        return (ObjectModel) getAbstractModel();
    }

    public Iterable<PropertyResolution> getPropertyResolutions()
    {
        return propertyResolutions;
    }
}