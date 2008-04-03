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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public final class CompositeDescriptor
{
    private CompositeModel compositeModel;
    private Map<Class, Serializable> compositeInfos;
    private Visibility visibility;

    public CompositeDescriptor( CompositeModel compositeModel, Map<Class, Serializable> compositeInfos, Visibility visibility )
    {
        this.compositeModel = compositeModel;
        this.compositeInfos = compositeInfos;
        this.visibility = visibility;
    }

    public CompositeModel getCompositeModel()
    {
        return compositeModel;
    }

    public Map<Class, Serializable> getCompositeInfos()
    {
        return compositeInfos;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }
}
