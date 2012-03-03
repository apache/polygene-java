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
package org.qi4j.api.entity;

import org.qi4j.api.association.AssociationMixin;
import org.qi4j.api.association.ManyAssociationMixin;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;

/**
 * Entity Composites must extend this interface.
 */
@Mixins( { AssociationMixin.class, ManyAssociationMixin.class } )
public interface EntityComposite
    extends Identity, Composite
{
}
