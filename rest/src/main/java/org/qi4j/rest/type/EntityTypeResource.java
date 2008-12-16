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
package org.qi4j.rest.type;

import java.io.StringWriter;
import java.util.Map;
import org.openrdf.model.Statement;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.rdf.entity.EntitySerializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.AssociationType;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationType;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.api.structure.Module;
import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class EntityTypeResource extends Resource
{
    @Uses EntitySerializer entitySerializer;

    @Structure Module module;
    @Structure Qi4jSPI spi;

    private String type;

    public EntityTypeResource( @Uses Context context,
                               @Uses Request request,
                               @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        setModifiable( false );

        final Map<String, Object> attributes = getRequest().getAttributes();
        type = (String) attributes.get( "type" );
    }

    @Override public Representation represent( final Variant variant )
        throws ResourceException
    {
        // Generate the right representation according to its media type.
        if( MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ) )
        {
            return representRdf();
        }
        else if( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            return representHtml();
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private Representation representRdf()
        throws ResourceException
    {
        EntityDescriptor entityDescriptor;
        try
        {
            entityDescriptor = spi.getEntityDescriptor( module.classLoader().loadClass( type ), module );
            if( entityDescriptor == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }
        catch( ClassNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        try
        {

            EntityType entityType = entityDescriptor.entityType();
            Iterable<Statement> statements = entitySerializer.serialize( entityType );

            StringWriter out = new StringWriter();
            new RdfXmlSerializer().serialize( statements, out );

            return new StringRepresentation( out.toString(), MediaType.APPLICATION_RDF_XML );
        }
        catch( Exception e )
        {
            throw new ResourceException( e );
        }
    }

    private Representation representHtml() throws ResourceException
    {
        EntityDescriptor entityDescriptor;
        try
        {
            entityDescriptor = spi.getEntityDescriptor( module.classLoader().loadClass( type ), module );
            if( entityDescriptor == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }
        catch( ClassNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        EntityType entityType = entityDescriptor.entityType();
        StringBuffer buf = new StringBuffer();
        buf.append( "<html><head><title>" + entityType.type() + "</title><link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" + entityType.type() + ".rdf\"/></head><body><h1>" + entityType.type() + "</h1>\n" );

        buf.append( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
        buf.append( "<fieldset><legend>Properties</legend>\n<table>" );
        for( PropertyType propertyType : entityType.properties() )
        {
            buf.append( "<tr><td>" +
                        "<label for=\"" + propertyType.qualifiedName() + "\" >" +
                        GenericPropertyInfo.getName( propertyType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><input " +
                        "type=\"text\" " +
                        "readonly=\"true\" " +
                        "name=\"" + propertyType.qualifiedName() + "\" " +
                        "value=\"" + propertyType.type() + "\"></td></tr>" );
        }
        buf.append( "</table></fieldset>\n" );

        buf.append( "<fieldset><legend>Associations</legend>\n<table>" );
        for( AssociationType associationType : entityType.associations() )
        {
            buf.append( "<tr><td>" +
                        "<label for=\"" + associationType.qualifiedName() + "\" >" +
                        GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><input " +
                        "type=\"text\" " +
                        "readonly=\"true\" " +
                        "size=\"40\" " +
                        "name=\"" + associationType.qualifiedName() + "\" " +
                        "value=\"" + associationType.type() + "\"></td></tr>" );
        }
        buf.append( "</table></fieldset>\n" );

        buf.append( "<fieldset><legend>Many associations</legend>\n<table>" );
        for( ManyAssociationType associationType : entityType.manyAssociations() )
        {
            buf.append( "<tr><td>" +
                        "<label for=\"" + associationType.qualifiedName() + "\" >" +
                        GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                        "</label></td>\n" +
                        "<td><input " +
                        "type=\"text\" " +
                        "name=\"" + associationType.qualifiedName() + "\" " +
                        "value=\"" + associationType.type() + "\"></td></tr>" );
        }
        buf.append( "</table></fieldset>\n" );

        buf.append( "</body></html>\n" );

        return new StringRepresentation( buf, MediaType.TEXT_HTML, Language.ENGLISH );
    }

}
