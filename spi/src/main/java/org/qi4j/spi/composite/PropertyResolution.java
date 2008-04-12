/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.composite;

import java.io.Serializable;
import org.qi4j.spi.property.PropertyModel;

/**
 * Property resolution. Mostly a placeholder for the moment.
 */
public final class PropertyResolution
    implements Serializable
{
    private PropertyModel propertyModel;
    private ConstraintsResolution constraintsResolution;

    public PropertyResolution( PropertyModel propertyModel, ConstraintsResolution constraintsResolution )
    {
        this.propertyModel = propertyModel;
        this.constraintsResolution = constraintsResolution;
    }

    public PropertyModel getPropertyModel()
    {
        return propertyModel;
    }

    public ConstraintsResolution getConstraintsResolution()
    {
        return constraintsResolution;
    }

    @Override public String toString()
    {
        return propertyModel.toString();
    }
}
