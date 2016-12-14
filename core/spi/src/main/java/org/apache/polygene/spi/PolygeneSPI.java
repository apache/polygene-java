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

package org.apache.polygene.spi;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.spi.entity.EntityState;

/**
 * Encapsulation of the Polygene SPI. This is implemented by the runtime.
 */
public interface PolygeneSPI
    extends PolygeneAPI
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
    EntityReference entityReferenceOf( Association<?> assoc );

    /**
     * Fetches the EntityReferences without loading the referenced entities.
     *
     * @param assoc The ManyAssociation for which we want to obtain the EntityReferences.
     * @return A stream of all the EntityReferences of the given ManyAssociation.
     */
    Stream<EntityReference> entityReferencesOf( ManyAssociation<?> assoc );

    /**
     * Fetches the EntityReferences without loading the referenced entities.
     *
     * @param assoc The NamedAssociation for which we want to obtain the EntityReference
     * @return A stream of Map.Entry with the names and EntityReferences of the given NamedAssociation.
     */
    Stream<Map.Entry<String, EntityReference>> entityReferencesOf( NamedAssociation<?> assoc );
}
