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

package org.qi4j.spi.injection;

import org.qi4j.spi.composite.AbstractResolution;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.structure.ApplicationResolution;
import org.qi4j.spi.structure.LayerResolution;
import org.qi4j.spi.structure.ModuleResolution;

/**
 * TODO
 */
public final class BindingContext
{
    private InjectionResolution injectionResolution;
    private AbstractResolution abstractResolution;
    private CompositeResolution compositeResolution;
    private ModuleResolution moduleResolution;
    private LayerResolution layerResolution;
    private ApplicationResolution applicationResolution;

    public BindingContext( InjectionResolution injectionResolution, AbstractResolution abstractResolution, CompositeResolution compositeResolution, ModuleResolution moduleResolution, LayerResolution layerResolution, ApplicationResolution applicationResolution )
    {
        this.injectionResolution = injectionResolution;
        this.abstractResolution = abstractResolution;
        this.compositeResolution = compositeResolution;
        this.moduleResolution = moduleResolution;
        this.layerResolution = layerResolution;
        this.applicationResolution = applicationResolution;
    }

    public BindingContext( InjectionResolution injectionResolution, BindingContext bindingContext )
    {
        copy( bindingContext );
        this.injectionResolution = injectionResolution;
    }

    public BindingContext( AbstractResolution abstractResolution, BindingContext bindingContext )
    {
        copy( bindingContext );
        this.abstractResolution = abstractResolution;
    }

    public ApplicationResolution getApplicationResolution()
    {
        return applicationResolution;
    }

    public LayerResolution getLayerResolution()
    {
        return layerResolution;
    }

    public ModuleResolution getModuleResolution()
    {
        return moduleResolution;
    }

    public CompositeResolution getCompositeResolution()
    {
        return compositeResolution;
    }

    public AbstractResolution getAbstractResolution()
    {
        return abstractResolution;
    }

    public InjectionResolution getInjectionResolution()
    {
        return injectionResolution;
    }

    private void copy( BindingContext bindingContext )
    {
        applicationResolution = bindingContext.getApplicationResolution();
        layerResolution = bindingContext.getLayerResolution();
        moduleResolution = bindingContext.getModuleResolution();
        compositeResolution = bindingContext.getCompositeResolution();
        abstractResolution = bindingContext.getAbstractResolution();
        injectionResolution = bindingContext.getInjectionResolution();
    }
}
