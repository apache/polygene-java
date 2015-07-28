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

import java.util.List;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.module.ModelModule;

import static org.qi4j.functional.Iterables.*;

/**
 * JAVADOC
 */
public final class UsedLayersInstance
{
    private final List<LayerInstance> usedLayerInstances;

    public UsedLayersInstance( List<LayerInstance> usedLayerInstances )
    {
        this.usedLayerInstances = usedLayerInstances;
    }

    /* package */ Iterable<ModelModule<ObjectDescriptor>> visibleObjects()
    {
        return flattenIterables( map( new Function<LayerInstance, Iterable<ModelModule<ObjectDescriptor>>>()
        {
            @Override
            public Iterable<ModelModule<ObjectDescriptor>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleObjects( Visibility.application );
            }
        }, usedLayerInstances ) );
    }

    /* package */ Iterable<ModelModule<TransientDescriptor>> visibleTransients()
    {
        return flattenIterables( map( new Function<LayerInstance, Iterable<ModelModule<TransientDescriptor>>>()
        {
            @Override
            public Iterable<ModelModule<TransientDescriptor>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleTransients( Visibility.application );
            }
        }, usedLayerInstances ) );
    }

    /* package */ Iterable<ModelModule<EntityDescriptor>> visibleEntities()
    {
        return flattenIterables( map( new Function<LayerInstance, Iterable<ModelModule<EntityDescriptor>>>()
        {
            @Override
            public Iterable<ModelModule<EntityDescriptor>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleEntities( Visibility.application );
            }
        }, usedLayerInstances ) );
    }

    /* package */ Iterable<ModelModule<ValueDescriptor>> visibleValues()
    {
        return flattenIterables( map( new Function<LayerInstance, Iterable<ModelModule<ValueDescriptor>>>()
        {
            @Override
            public Iterable<ModelModule<ValueDescriptor>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleValues( Visibility.application );
            }
        }, usedLayerInstances ) );
    }

    /* package */ Iterable<ServiceReference<?>> visibleServices()
    {
        return flattenIterables( map( new Function<LayerInstance, Iterable<ServiceReference<?>>>()
        {
            @Override
            public Iterable<ServiceReference<?>> map( LayerInstance layerInstance )
            {
                return layerInstance.visibleServices( Visibility.application );
            }
        }, usedLayerInstances ) );
    }
}
