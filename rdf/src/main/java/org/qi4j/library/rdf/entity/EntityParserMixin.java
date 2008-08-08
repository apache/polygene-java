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

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.qi4j.entity.Identity;
import org.qi4j.entity.association.GenericAssociationInfo;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.structure.Module;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public class EntityParserMixin
    implements EntityParser
{
    private @Structure Module module;
    private @Structure Qi4jSPI spi;

    private URI identityUri;

    public EntityParserMixin()
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        identityUri = values.createURI( GenericPropertyInfo.toURI( Identity.class, "identity" ) );
    }

    public void parse( Iterable<Statement> entityGraph, EntityState entityState )
    {
        Map<String, String> propertyValues = new HashMap<String, String>();
        Map<String, QualifiedIdentity> associationValues = new HashMap<String, QualifiedIdentity>();
        String className = null;
        String id = null;
        for( Statement statement : entityGraph )
        {
            if( statement.getPredicate().equals( Rdfs.TYPE ) )
            {
                className = ClassUtil.toClassName( statement.getObject().toString() );
            }
            else if( statement.getPredicate().equals( identityUri ) )
            {
                id = statement.getObject().stringValue();
            }
            else
            {
                URI predicate = statement.getPredicate();
                Value object = statement.getObject();
                if( object instanceof URI )
                {
                    String qualifiedName = GenericAssociationInfo.toQualifiedName( predicate.toString() );
                    String str = object.stringValue().substring( "urn:qi4j:".length() );
                    String[] strings = str.split( "/" );
                    String type = strings[ 0 ].replace( "-", "$" );
                    String identity = strings[ 1 ];
                    QualifiedIdentity qid = new QualifiedIdentity( identity, type );
                    associationValues.put( qualifiedName, qid );
                }
                else
                {
                    String qualifiedName = GenericPropertyInfo.toQualifiedName( predicate.toString() );
                    propertyValues.put( qualifiedName, object.stringValue() );
                }
            }
        }

        if( className == null || id == null )
        {
            return;
        }

        for( Map.Entry<String, String> propertyEntry : propertyValues.entrySet() )
        {
            entityState.setProperty( propertyEntry.getKey(), propertyEntry.getValue() );
        }

        for( Map.Entry<String, QualifiedIdentity> associationEntry : associationValues.entrySet() )
        {
            entityState.setAssociation( associationEntry.getKey(), associationEntry.getValue() );
        }
    }
}