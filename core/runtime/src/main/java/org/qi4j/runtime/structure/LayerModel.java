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

package org.qi4j.runtime.structure;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;

import java.util.List;

/**
 * JAVADOC
 */
public final class LayerModel
    implements LayerDescriptor, VisitableHierarchy<Object, Object>
{
    // Model
    private final String name;
    private MetaInfo metaInfo;
    private final UsedLayersModel usedLayersModel;
    private final List<ModuleModel> modules;

    public LayerModel( String name,
                       MetaInfo metaInfo,
                       UsedLayersModel usedLayersModel,
                       List<ModuleModel> modules
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.usedLayersModel = usedLayersModel;
        this.modules = modules;
    }

    public String name()
    {
        return name;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public Iterable<ModuleModel> modules()
    {
        return modules;
    }

    public UsedLayersModel usedLayers()
    {
        return usedLayersModel;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
        {
            for( ModuleModel module : modules )
            {
                if (!module.accept( modelVisitor ))
                    break;
            }
        }
        return modelVisitor.visitLeave( this );
    }

    // Context
    public LayerInstance newInstance( ApplicationInstance applicationInstance, UsedLayersInstance usedLayerInstance )
    {
        LayerInstance layerInstance = new LayerInstance( this, applicationInstance, usedLayerInstance );
        for( ModuleModel module : modules )
        {
            ModuleInstance moduleInstance = module.newInstance( layerInstance );
            layerInstance.addModule( moduleInstance );
        }

        return layerInstance;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
