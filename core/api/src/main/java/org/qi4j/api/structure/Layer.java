/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman.
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

package org.qi4j.api.structure;

import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivationEventListenerRegistration;

/**
 * The Layer represents a single layer in a Qi4j application.
 */
public interface Layer
    extends ActivationEventListenerRegistration, Activation, MetaInfoHolder
{
    /**
     * @return the Layer's name
     */
    String name();
}
