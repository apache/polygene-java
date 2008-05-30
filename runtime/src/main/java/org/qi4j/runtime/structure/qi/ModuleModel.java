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
import org.qi4j.composite.AmbiguousMixinTypeException;
import org.qi4j.runtime.composite.qi.CompositeModel;
import org.qi4j.runtime.composite.qi.Resolution;

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
    public void bind( Resolution resolution )
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), this, null, null );
        for( CompositeModel compositeComposite : composites )
        {
            compositeComposite.bind( resolution );
        }
    }

    // Context
    public ModuleInstance newInstance( LayerInstance layerInstance )
    {
        ModuleInstance moduleInstance = new ModuleInstance( this, layerInstance );

        return moduleInstance;
    }

    public String name()
    {
        return null;
    }

    public CompositeModel getCompositeModelFor( Class mixinType )
    {
        CompositeModel foundModel = null;
        for( CompositeModel composite : composites )
        {
            if( mixinType.isAssignableFrom( composite.type() ) )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousMixinTypeException( mixinType );
                }
                else
                {
                    foundModel = composite;
                }
            }
        }

        return foundModel;
    }
}
