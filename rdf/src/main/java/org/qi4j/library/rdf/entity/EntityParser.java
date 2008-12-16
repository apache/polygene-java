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
import org.qi4j.api.entity.Identity;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.api.util.ClassUtil;

/**
 * TODO
 */
public class EntityParser
{
    private String identityUri;

    public EntityParser()
    {
        identityUri = GenericPropertyInfo.toURI( Identity.class, "identity" );
    }

    public void parse( Iterable<Statement> entityGraph, EntityState entityState )
    {
        Map<String, String> propertyValues = new HashMap<String, String>();
        Map<String, QualifiedIdentity> associationValues = new HashMap<String, QualifiedIdentity>();
        Map<String, BNode> manyAssociationResources = new HashMap<String, BNode>();
        Map<BNode, Collection<QualifiedIdentity>> manyAssociationValues = new HashMap<BNode, Collection<QualifiedIdentity>>();

        String className = null;
        for( Statement statement : entityGraph )
        {
            if( statement.getPredicate().equals( Rdfs.TYPE ) )
            {
                className = ClassUtil.toClassName( statement.getObject().toString() );
            }
            else
            {
                Resource subject = statement.getSubject();
                URI predicate = statement.getPredicate();
                Value object = statement.getObject();
                if( subject instanceof BNode )
                {
                    // ManyAssociation item
                    String uri = predicate.toString();

                    Collection<QualifiedIdentity> manyAssociation = manyAssociationValues.get( subject );
                    manyAssociation.add( QualifiedIdentity.parseURI( object.stringValue() ) );
                }
                else if( object instanceof URI )
                {
                    // Association
                    String uri = predicate.toString();
                    QualifiedIdentity qid = QualifiedIdentity.parseURI( object.stringValue() );
                    associationValues.put( uri, qid );
                }
                else if( object instanceof BNode )
                {
                    // ManyAssociation
                    String uri = predicate.toString();
                    manyAssociationResources.put( uri, (BNode) object );
                    manyAssociationValues.put( (BNode) object, new ArrayList<QualifiedIdentity>() );
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

        if( className == null || id == null )
        {
            return;
        }

        for( PropertyType propertyType : entityState.entityType().properties() )
        {
            entityState.setProperty( propertyType.qualifiedName(), propertyValues.get( propertyType.uri() ) );
        }

        for( AssociationType associationType : entityState.entityType().associations() )
        {
            QualifiedIdentity entity = associationValues.get( associationType.uri() );
            if( entity != null )
            {
                entityState.setAssociation( associationType.qualifiedName(), entity );
            }
        }

        for( ManyAssociationType manyAssociationType : entityState.entityType().manyAssociations() )
        {
            Collection<QualifiedIdentity> entities = manyAssociationValues.get( manyAssociationResources.get( manyAssociationType.uri() ) );
            if( entities != null )
            {
                Collection<QualifiedIdentity> stateEntities = entityState.getManyAssociation( manyAssociationType.qualifiedName() );
                stateEntities.clear();
                stateEntities.addAll( entities );
            }
        }
    }
}