/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.model;

import java.lang.reflect.Field;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.structure.ApplicationModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;

/**
 * JAVADOC
 */
public final class Resolution
{
    private final ApplicationModel application;
    private final LayerModel layer;
    private final ModuleModel module;
    private final ModelDescriptor modelDescriptor;
    private final CompositeMethodModel method;
    private final Field field;

    public Resolution( ApplicationModel application,
                       LayerModel layer,
                       ModuleModel module,
                       ModelDescriptor modelDescriptor,
                       CompositeMethodModel method,
                       Field field
    )
    {
        this.application = application;
        this.layer = layer;
        this.module = module;
        this.modelDescriptor = modelDescriptor;
        this.method = method;
        this.field = field;
    }

    public ApplicationModel application()
    {
        return application;
    }

    public LayerModel layer()
    {
        return layer;
    }

    public ModuleModel module()
    {
        return module;
    }

    public ModelDescriptor model()
    {
        return modelDescriptor;
    }

    public CompositeMethodModel method()
    {
        return method;
    }

    public Field field()
    {
        return field;
    }

    public Resolution forField( final Field injectedField )
    {
        return new Resolution( application, layer, module, modelDescriptor, method, injectedField );
    }
}
