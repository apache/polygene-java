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

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singleton;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.qi4j.entity.Identity;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.property.AbstractPropertyInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.structure.Module;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public class EntitySerializerMixin
    implements EntitySerializer
{
    private @Service EntityStore entityStore;
    private @Structure Module module;

    private URI identityUri;

    public EntitySerializerMixin()
    {
        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        identityUri = values.createURI( AbstractPropertyInstance.toURI( Identity.class, "identity" ) );
    }

    public Iterable<Statement> serialize( QualifiedIdentity qid )
    {
        EntityState entityState = entityStore.getEntityState( null, qid );

        Graph graph = new GraphImpl();
        ValueFactory values = graph.getValueFactory();
        URI entityUri = values.createURI( "urn:qi4j:" + qid.identity() );

        graph.add( entityUri, Rdfs.TYPE, values.createURI( ClassUtil.toURI( qid.type() ) ) );
        graph.add( entityUri, identityUri, values.createLiteral( qid.identity() ) );

        // Properties
        for( String propertyName : entityState.propertyNames() )
        {
            Object value = entityState.getProperty( propertyName );
            URI propertyUri = values.createURI( AbstractPropertyInstance.toURI( propertyName ) );
            graph.add( entityUri, propertyUri, values.createLiteral( value.toString() ) );
        }

        return graph;
    }

    public void parse( Iterable<Statement> entityGraph )
    {
        Map<String, String> propertyValues = new HashMap<String, String>();
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
                String qualifiedName = AbstractPropertyInstance.toQualifiedName( statement.getPredicate().toString() );
                propertyValues.put( qualifiedName, statement.getObject().stringValue() );
            }
        }

        if( className == null || id == null )
        {
            return;
        }

        QualifiedIdentity qid = new QualifiedIdentity( id, className );
        EntityState entityState = entityStore.getEntityState( null, qid );
        for( Map.Entry<String, String> propertyEntry : propertyValues.entrySet() )
        {
            entityState.setProperty( propertyEntry.getKey(), propertyEntry.getValue() );
        }

        entityStore.prepare( EMPTY_LIST, singleton( entityState ), EMPTY_LIST, module ).commit();
    }
}
