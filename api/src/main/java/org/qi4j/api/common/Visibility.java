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
package org.qi4j.api.common;

/**
 * Visibility is a core concept in the Qi4j structure system. It defines the locale of composites and objects, i.e.
 * how far they can be 'seen' and therefor be used.
 * <p>
 * When a Composite or Object is declared in the assembly phase, and no visibility is set, only other
 * composites/objects within the same module can use that declaration. For a declared composite/object to be usable
 * from other modules a higher visibility must be set, either that the Composite/Object can be used by others within
 * the same Layer, or even to be used by those in the layer above.
 * </p>
 */
public enum Visibility
{
    /**
     * Artifact is visible only in the declaring module (default)
     */
    module,
    /**
     * Artifact is visible to all modules in the same layer
     */
    layer,
    /**
     * Artifact is visible to other modules in the same layer and any modules in extending layers
     */
    application
}
