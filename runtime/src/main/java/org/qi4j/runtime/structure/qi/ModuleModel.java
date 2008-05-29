/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.structure.qi;

import java.util.List;
import org.qi4j.runtime.composite.qi.BindingContext;
import org.qi4j.runtime.composite.qi.CompositeModel;

/**
 * TODO
 */
public class ModuleModel
{
    private List<CompositeModel> composites;

    private LayerModel layerComposite;

    public ModuleModel( LayerModel layerComposite, List<CompositeModel> composites )
    {
        this.layerComposite = layerComposite;
        this.composites = composites;
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        bindingContext = new BindingContext( bindingContext.application(), bindingContext.layer(), this, null );
        for( CompositeModel compositeComposite : composites )
        {
            compositeComposite.bind( bindingContext );
        }
    }

    // Context
    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        ModuleInstance moduleInstance = new ModuleInstance( this, layerInstance );

        return moduleInstance;
    }

}
