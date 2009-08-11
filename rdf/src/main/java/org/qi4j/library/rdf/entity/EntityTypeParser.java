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

import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.OWL;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Qi4jEntityType;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

/**
 * Parser for RDF to EntityType.
 */
public class EntityTypeParser
{
    public EntityTypeParser()
    {
    }

    public EntityType parse( Iterable<Statement> entityTypeGraph )
    {
        TypeName type = null;
        String uri = null;
        String rdf = null;
        boolean queryable = true;
        Set<String> mixinTypes = new HashSet<String>();
        Set<PropertyType> properties = new HashSet<PropertyType>();
        Set<AssociationType> associations = new HashSet<AssociationType>();
        Set<ManyAssociationType> manyAssociations = new HashSet<ManyAssociationType>();

        for( Statement statement : entityTypeGraph )
        {
            if( statement.getPredicate().equals( Rdfs.TYPE ) )
            {

                String str = statement.getObject().toString();
                if (!str.equals(Rdfs.CLASS.toString()) && !str.equals(OWL.CLASS.toString()))
                    rdf = str;
                uri = statement.getSubject().stringValue();
            }
            else if( statement.getPredicate().equals( Qi4jEntityType.TYPE ) )
            {
                type = TypeName.nameOf(Classes.toClassName(statement.getObject().stringValue()));
            }
            else if( statement.getPredicate().equals( Qi4jEntityType.QUERYABLE ) )
            {
                queryable = Boolean.parseBoolean( statement.getObject().stringValue() );
            }

/*

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
*/
        }

        EntityType entityType = new EntityType(type, queryable, mixinTypes, properties, associations, manyAssociations);
        return entityType;
    }
}