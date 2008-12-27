/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.swing.binding.example.dom;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.framework.DecoratorMixin;
import org.qi4j.library.swing.binding.tree.ContainerMixin;
import org.qi4j.library.swing.binding.tree.Child;
import org.qi4j.library.swing.binding.tree.Container;
import org.qi4j.library.swing.binding.tree.Descriptor;
import org.qi4j.library.swing.binding.tree.TreeMixins;

@Concerns( NodeListingContainerConcern.class )
@Mixins( { NodeDescriptorMixin.class, ContainerMixin.class, DecoratorMixin.class } )
public interface ElementComposite extends Child, Container, Descriptor, TreeMixins, Composite
{
}
