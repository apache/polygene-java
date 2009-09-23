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

package org.qi4j.library.rdf.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.library.rdf.Qi4jEntity;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

/**
 * JAVADOC
 */
public class EntityStateParser
{
    private String identityUri;

    public EntityStateParser()
    {
        identityUri = QualifiedName.fromClass( Identity.class, "identity" ).toURI();
    }

    public void parse( Iterable<Statement> entityGraph, EntityState entityState )
    {
        Map<String, String> propertyValues = new HashMap<String, String>();
        Map<String, EntityReference> associationValues = new HashMap<String, EntityReference>();
        Map<String, BNode> manyAssociationResources = new HashMap<String, BNode>();
        Map<BNode, Collection<EntityReference>> manyAssociationValues = new HashMap<BNode, Collection<EntityReference>>();

        String entityTypeReference = null;
        for( Statement statement : entityGraph )
        {
            if( statement.getPredicate().equals( Qi4jEntity.ENTITYTYPEREFERENCE ) )
            {
                entityTypeReference = statement.getObject().toString();
            }
            else
            {
                Resource subject = statement.getSubject();
                URI predicate = statement.getPredicate();
                Value object = statement.getObject();
                if( subject instanceof BNode
                    && object instanceof URI )
                {
                    // ManyAssociation item
                    String uri = predicate.toString();

                    BNode key = (BNode) subject;
                    Collection<EntityReference> manyAssociation = manyAssociationValues.get( key );
                    manyAssociation.add( EntityReference.parseURI( object.stringValue() ) );
                }
                else if( object instanceof URI )
                {
                    // Association
                    String uri = predicate.toString();
                    EntityReference qid = EntityReference.parseURI( object.stringValue() );
                    associationValues.put( uri, qid );
                }
                else if( object instanceof BNode )
                {
                    // ManyAssociation
                    String uri = predicate.toString();
                    manyAssociationResources.put( uri, (BNode) object );
                    manyAssociationValues.put( (BNode) object, new ArrayList<EntityReference>() );
                }
                else
                {
                    // Property
                    String uri = predicate.toString();
                    propertyValues.put( uri, object.stringValue() );
                }
            }
        }

        String id = propertyValues.get( identityUri );

        if( entityTypeReference == null || id == null )
        {
            return;
        }

        EntityType entityType = entityState.entityDescriptor().entityType();

        for( PropertyType propertyType : entityType.properties() )
        {
            String json = propertyValues.get( propertyType.qualifiedName().toURI() );

            if( propertyType.type().isString() ||
                propertyType.type().isEnum() )
            {
                // Add "" around string
                json = "\"" + json + "\"";
            }

            entityState.setProperty( propertyType.qualifiedName(), json );
        }

        for( AssociationType associationType : entityType.associations() )
        {
            EntityReference entity = associationValues.get( associationType.qualifiedName().toURI() );
            if( entity != null )
            {
                entityState.setAssociation( associationType.qualifiedName(), entity );
            }
        }

        for( ManyAssociationType manyAssociationType : entityType.manyAssociations() )
        {
            Collection<EntityReference> entities = manyAssociationValues.get( manyAssociationResources.get( manyAssociationType.qualifiedName().toURI() ) );
            if( entities != null )
            {
                ManyAssociationState stateEntities = entityState.getManyAssociation( manyAssociationType.qualifiedName() );
                while( stateEntities.count() > 0 )
                {
                    stateEntities.remove( stateEntities.get( 0 ) );
                }

                int idx = 0;
                for( EntityReference entity : entities )
                {
                    stateEntities.add( idx++, entity );
                }
            }
        }
    }
}