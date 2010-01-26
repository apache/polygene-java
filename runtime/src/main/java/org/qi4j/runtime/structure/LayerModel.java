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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.spi.structure.LayerDescriptor;

/**
 * JAVADOC
 */
public final class LayerModel
    implements Binder, LayerDescriptor, Serializable
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

    public UsedLayersModel usedLayers()
    {
        return usedLayersModel;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        for( ModuleModel module : modules )
        {
            module.visitModel( modelVisitor );
        }
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), this, null, null, null, null );
        for( ModuleModel module : modules )
        {
            module.bind( resolution );
        }
    }

    // Context

    public TransientModel findCompositeFor( Class mixinType, Visibility visibility )
    {
        // Check this layer
        TransientModel foundModel = null;
        for( ModuleModel model : modules )
        {
            TransientModel transientModel = model.composites().getCompositeModelFor( mixinType, visibility );
            if( transientModel != null )
            {
                if( foundModel != null )
                {
                    throw new AmbiguousTypeException( mixinType, foundModel.type(), transientModel.type() );
                }
                else
                {
                    foundModel = transientModel;
                }
            }
        }

        if( foundModel != null )
        {
            return foundModel;
        }

        if( visibility == Visibility.layer )
        {
            // Check application scope
            foundModel = findCompositeFor( mixinType, Visibility.application );
            if( foundModel != null )
            {
                return foundModel;
            }
            else
            {
                return usedLayersModel.findCompositeFor( mixinType );
            }
        }
        else
        {
            return null;
        }
    }

    public LayerInstance newInstance( ApplicationInstance applicationInstance, UsedLayersInstance usedLayerInstance )
    {
        List<ModuleInstance> moduleInstances = new ArrayList<ModuleInstance>();
        LayerInstance layerInstance = new LayerInstance( this, applicationInstance, moduleInstances, usedLayerInstance );
        for( ModuleModel module : modules )
        {
            ModuleInstance moduleInstance = module.newInstance( layerInstance );
            moduleInstances.add( moduleInstance );
        }

        return layerInstance;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean visitModules( ModuleVisitor visitor, Visibility visibility )
    {
        // Visit modules in this layer
        ModuleInstance foundModule = null;
        for( ModuleModel moduleModel : modules )
        {
            if( !visitor.visitModule( null, moduleModel, visibility ) )
            {
                return false;
            }
        }

        if( visibility == Visibility.layer )
        {
            // Visit modules in this layer
            if( !visitModules( visitor, Visibility.application ) )
            {
                return false;
            }

            // Visit modules in used layers
            return usedLayersModel.visitModules( visitor );
        }

        return true;
    }
}
