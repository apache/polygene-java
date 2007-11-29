/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

/**
 * TODO
 */
public class PropertyResolution
    implements Serializable
{
    private PropertyModel propertyModel;
    private MixinResolution mixinResolution;

    public PropertyResolution( PropertyModel propertyModel, MixinResolution mixinResolution )
    {
        this.propertyModel = propertyModel;
        this.mixinResolution = mixinResolution;
    }

    public PropertyModel getPropertyModel()
    {
        return propertyModel;
    }

    public MixinResolution getMixinResolution()
    {
        return mixinResolution;
    }
}
