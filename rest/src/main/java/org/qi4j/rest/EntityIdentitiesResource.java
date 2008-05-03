/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.rest;

import java.io.IOException;
import java.util.Map;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.structure.Module;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityIdentitiesResource extends Resource
{

    @Structure private UnitOfWorkFactory unitOfWorkFactory;

    private Class type;

    public EntityIdentitiesResource( @Uses Context context,
                                     @Uses Request request,
                                     @Uses Response response,
                                     @Structure Module module )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        setModifiable( false );

        final Map<String, Object> attributes = getRequest().getAttributes();
        String typeAttr = (String) attributes.get( "type" );
        try
        {
            type = module.lookupClass( typeAttr );
        }
        catch( ClassNotFoundException e )
        {
            // TODO Errorhandling 
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    @Override public Representation represent( final Variant variant )
        throws ResourceException
    {
        final UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        final QueryBuilderFactory qbf = unitOfWork.queryBuilderFactory();
        final QueryBuilder queryBuilder = qbf.newQueryBuilder( type );
        Query query = queryBuilder.newQuery();
        // Generate the right representation according to its media type.
        if( MediaType.TEXT_XML.equals( variant.getMediaType() ) )
        {
            try
            {
                DomRepresentation representation = new DomRepresentation( MediaType.TEXT_XML );
                // Generate a DOM document representing the item.
                Document d = representation.getDocument();

                Element entitiesElement = d.createElement( "entities" );
                d.appendChild( entitiesElement );
                for( Object entity : query )
                {
                    EntityComposite entityComposite = (EntityComposite) entity;
                    Element entityElement = d.createElement( "entity" );
                    entitiesElement.appendChild( entityElement );
                    entityElement.setAttribute( "href", "/entity/" + entityComposite.type().getName() + "/" + entityComposite.identity().get() );
                    entityElement.appendChild( d.createTextNode( entityComposite.identity().get() ) );
                }
                d.normalizeDocument();

                // Returns the XML representation of this document.
                return representation;
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
        return null;
    }
}
