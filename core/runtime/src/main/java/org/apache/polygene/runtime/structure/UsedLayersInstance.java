/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.structure;

import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.structure.LayerDescriptor;
import org.apache.polygene.api.value.ValueDescriptor;

/**
 * JAVADOC
 */
public final class UsedLayersInstance
{
    private final List<LayerDescriptor> usedLayerInstances;

    public UsedLayersInstance( List<LayerDescriptor> usedLayerInstances )
    {
        this.usedLayerInstances = usedLayerInstances;
    }

    Stream<? extends ObjectDescriptor> visibleObjects()
    {
        return usedLayerInstances.stream()
            .flatMap( layerInstance -> layerInstance.visibleObjects( Visibility.application ) );
    }

    Stream<? extends TransientDescriptor> visibleTransients()
    {
        return usedLayerInstances.stream()
            .flatMap( layerInstance -> layerInstance.visibleTransients( Visibility.application ) );
    }

    Stream<? extends EntityDescriptor> visibleEntities()
    {
        return usedLayerInstances.stream()
            .flatMap( layerInstance -> layerInstance.visibleEntities( Visibility.application ) );
    }

    Stream<? extends ValueDescriptor> visibleValues()
    {
        return usedLayerInstances.stream()
            .flatMap( layerInstance -> layerInstance.visibleValues( Visibility.application ) );
    }

    Stream<? extends ModelDescriptor> visibleServices()
    {
        return usedLayerInstances.stream()
            .flatMap( layerInstance -> layerInstance.visibleServices( Visibility.application ) );
    }
}
