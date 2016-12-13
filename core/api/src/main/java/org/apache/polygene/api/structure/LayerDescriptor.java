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
package org.apache.polygene.api.structure;

import java.util.stream.Stream;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.ModelDescriptor;
import org.apache.polygene.api.composite.TransientDescriptor;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.object.ObjectDescriptor;
import org.apache.polygene.api.value.ValueDescriptor;

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