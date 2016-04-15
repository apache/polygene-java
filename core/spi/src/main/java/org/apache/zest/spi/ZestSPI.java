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

package org.apache.zest.spi;

import java.util.Map;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.spi.entity.EntityState;

/**
 * Encapsulation of the Zest SPI. This is implemented by the runtime.
 */
public interface ZestSPI
    extends ZestAPI
{
    StateHolder stateOf( TransientComposite composite );

    AssociationStateHolder stateOf( EntityComposite composite );

    AssociationStateHolder stateOf( ValueComposite composite );

    // Entities
    EntityState entityStateOf( EntityComposite composite );

    /**
     * Fetches the EntityReference without loading the referenced entity.
     *
     * @param assoc The Association for which we want to obtain the EntityReference
     * @return The EntityReference of the given Association.
     */
    EntityReference entityReferenceOf( Association assoc );

    /**
     * Fetches the EntityReferences without loading the referenced entities.
     *
     * @param assoc The ManyAssociation for which we want to obtain the EntityReferences.
     * @return An Iteranble of all the EntityReferences of the given ManyAssociation.
     */
    Iterable<EntityReference> entityReferenceOf( ManyAssociation assoc );

    /**
     * Fetches the EntityReferences without loading the referenced entities.
     *
     * @param assoc The NamedAssociation for which we want to obtain the EntityReference
     * @return An Iteranble of Map.Entry with the name and EntityReference of the given NamedAssociation.
     */
    Iterable<Map.Entry<String,EntityReference>> entityReferenceOf( NamedAssociation assoc );

}
