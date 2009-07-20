/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.entity;

import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.api.entity.EntityReference;

import java.io.PrintWriter;

/**
 * JAVADOC
 */
public class EntityStoreExporter
{
    public void exportEntityStore(EntityStore store, final EntityTypeRegistry registry, final PrintWriter out)
    {
        out.append( "[" );

        store.visitEntityStates( new EntityStore.EntityStateVisitor()
        {
            public void visitEntityState( EntityState entityState )
            {
                out.append( "{" );
                out.append( "identity:" ).append( entityState.identity().identity() );
                out.append( ",version:" ).append( entityState.version() );
                for( EntityTypeReference entityTypeReference : entityState.entityTypeReferences() )
                {
                    out.append( ",type:" ).append( entityTypeReference.type().normalized() );
                    EntityType type = registry.getEntityType( entityTypeReference );
                    for( PropertyType propertyType : type.properties() )
                    {
                        out.append(",").append( propertyType.qualifiedName().name() ).append( ":" ).append( entityState.getProperty(propertyType.stateName() ));
                    }
                    for( AssociationType associationType : type.associations() )
                    {
                        EntityReference association = entityState.getAssociation( associationType.stateName() );
                        if (association != null)
                            out.append( associationType.qualifiedName().name()).append(":").append( association.identity() );
                    }
                }
                out.append("}").println();
            }
        });

        out.append("]");
    }
}
