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

package org.qi4j.spi.dependency;

import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.ObjectModel;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

/**
 * TODO
 */
public class ResolutionContext
{
    private ObjectModel objectModel;
    private CompositeModel compositeModel;
    private ModuleModel module;
    private LayerModel layer;
    private ApplicationModel application;

    public ResolutionContext( ObjectModel objectModel, CompositeModel compositeModel, ModuleModel module, LayerModel layer, ApplicationModel application )
    {
        this.objectModel = objectModel;
        this.compositeModel = compositeModel;
        this.module = module;
        this.layer = layer;
        this.application = application;
    }

    public ObjectModel getObjectModel()
    {
        return objectModel;
    }

    public CompositeModel getCompositeModel()
    {
        return compositeModel;
    }

    public ModuleModel getModule()
    {
        return module;
    }

    public LayerModel getLayer()
    {
        return layer;
    }

    public ApplicationModel getApplication()
    {
        return application;
    }
}
