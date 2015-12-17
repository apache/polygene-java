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
package org.apache.zest.api.structure;

import java.util.stream.Stream;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.value.ValueDescriptor;

/**
 * Layer Descriptor.
 */
public interface LayerDescriptor
{

    /**
     * @return the Layer's name
     */
    String name();

    Layer instance();

    /**
     * @return Layers used by this Layer
     */
    UsedLayersDescriptor usedLayers();

    Stream<? extends ObjectDescriptor> visibleObjects( Visibility visibility );

    Stream<? extends TransientDescriptor> visibleTransients( Visibility visibility );

    Stream<? extends EntityDescriptor> visibleEntities( Visibility visibility );

    Stream<? extends ValueDescriptor> visibleValues( Visibility visibility );

    Stream<? extends ModelDescriptor> visibleServices( Visibility visibility );
}