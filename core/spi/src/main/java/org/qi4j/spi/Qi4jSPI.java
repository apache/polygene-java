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

package org.qi4j.spi;

import org.qi4j.api.Qi4j;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.entity.EntityState;

/**
 * Encapsulation of the Qi4j SPI. This is implemented by the runtime.
 */
public interface Qi4jSPI
    extends Qi4j
{
    StateHolder stateOf( TransientComposite composite );

    AssociationStateHolder stateOf( EntityComposite composite );

    AssociationStateHolder stateOf( ValueComposite composite );

    // Entities
    EntityState entityStateOf( EntityComposite composite );
}
