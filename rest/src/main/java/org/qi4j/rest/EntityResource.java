/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.rest;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.library.rdf.entity.EntitySerializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class EntityResource extends Resource
{
    @Service private EntityStore entityStore;
    @Structure private Qi4jSPI spi;
    @Service EntitySerializer entitySerializer;


    private EntityState entity;
    private QualifiedIdentity qualifiedIdentity;

    public EntityResource( @Uses Context context, @Uses Request request, @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        setModifiable( true );

        // Get the "itemName" attribute value taken from the URI template
        // /entity/{identity}.
        Map<String, Object> attributes = getRequest().getAttributes();
        String identity = (String) attributes.get( "identity" );
        String type = (String) attributes.get( "type" );
        String ext = request.getResourceRef().getExtensions();
        if( ext != null )
        {
            identity = identity.substring( 0, identity.length() - ext.length() - 1 );
        }
        qualifiedIdentity = new QualifiedIdentity( identity, type );
    }

    /**
     * Handle DELETE requests.
     */
    @Override
    public void removeRepresentations()
        throws ResourceException
    {
        try
        {
            entityStore.prepare( Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.singleton( qualifiedIdentity ) ).commit();
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch( EntityNotFoundException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }
    }

    @Override
    public Representation represent( Variant variant )
        throws ResourceException
    {
        if( entity == null )
        {
            try
            {
                entity = entityStore.getEntityState( qualifiedIdentity );
            }
            catch( UnknownEntityTypeException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }

        // Check modification date
        Date lastModified = getRequest().getConditions().getModifiedSince();
        if( lastModified != null )
        {
            if( lastModified.getTime() / 1000 == entity.lastModified() / 1000 )
            {
                throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED );
            }
        }

        // Generate the right representation according to its media type.
        String ext = getRequest().getResourceRef().getExtensions();
        if( "rdf".equals( ext ) ||
            MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ) )
        {
            return entityHeaders( representRdfXml( entity ) );
        }
        else if( "html".equals( ext ) ||
                 MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            return entityHeaders( representHtml( entity ) );
        }

        throw new

            ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private Representation entityHeaders( Representation representation )
    {
        representation.setModificationDate( new Date( entity.lastModified() ) );
        representation.setTag( new Tag( "" + entity.version() ) );

        return representation;
    }


    private Representation representHtml( EntityState entity )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "<html><body><h1>" + entity.qualifiedIdentity().identity() + "</h1><ul>" );

        buf.append( "<form method=\"POST\" action=\"" + getRequest().getResourceRef().getPath() + "\">" );
        buf.append( "<h2>Properties</h2><ul>" );
        for( PropertyType propertyType : entity.entityType().properties() )
        {
            if( propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE )
            {
                buf.append( "<li>" + propertyType.qualifiedName() + "<input type=\"text\" name=\"" + propertyType.qualifiedName() + "\" value=\"" + entity.getProperty( propertyType.qualifiedName() ) + "\">" );
            }
            else if( propertyType.propertyType() == PropertyType.PropertyTypeEnum.IMMUTABLE )
            {
                buf.append( "<li>" + propertyType.qualifiedName() + ":" + entity.getProperty( propertyType.qualifiedName() ) + "</li>" );
            }
        }
        buf.append( "</ul><input type=\"submit\" value=\"Update\"/></form></body></html>" );

// Returns the XML representation of this document.
        return new StringRepresentation( buf, MediaType.TEXT_HTML, Language.ENGLISH );
    }

    private Representation representRdfXml( EntityState entity ) throws ResourceException
    {
        try
        {
            Iterable<Statement> statements = entitySerializer.serialize( entity );
            StringWriter out = new StringWriter();
            new RdfXmlSerializer().serialize( statements, out );

            return new StringRepresentation( out.toString(), MediaType.APPLICATION_RDF_XML );
        }
        catch( RDFHandlerException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }

    @Override @SuppressWarnings( "unused" )
    public void acceptRepresentation( Representation entity ) throws ResourceException
    {
        storeRepresentation( entity );
    }

    /**
     * Handle PUT requests.
     */
    @Override
    public void storeRepresentation( Representation entityRepresentation )
        throws ResourceException
    {
        entity = entityStore.getEntityState( qualifiedIdentity );

        Form form = new Form( entityRepresentation );
        Map<String, String> values = form.getValuesMap();

        for( Map.Entry<String, String> formEntry : values.entrySet() )
        {
            entity.setProperty( formEntry.getKey(), formEntry.getValue() );
        }

        entityStore.prepare( Collections.EMPTY_LIST, Collections.singleton( entity ), Collections.EMPTY_LIST ).commit();

        getResponse().setStatus( Status.SUCCESS_RESET_CONTENT );
    }

    @Override public boolean isModifiable()
    {
        return true;
    }

    /*
        private Property getProperty( PropertyBinding propertyBinding )
        {
            Method method = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor();
            Property property;
            try
            {
                property = (Property) method.invoke( entity );
            }
            catch( IllegalAccessException e )
            {
                // Can not happen.
                throw new InternalError();
            }
            catch( InvocationTargetException e )
            {
                //TODO  What to do?
                e.printStackTrace();
                throw new InternalError();
            }
            return property;
        }
    */

/*
    private Association<?> getAssociation( final AssociationBinding associationBinding )
    {
        final Method accessor = associationBinding.getAssociationResolution().getAssociationModel().getAccessor();
        Association<?> association;
        try
        {
            association = (Association<?>) accessor.invoke( entity );
        }
        catch( IllegalAccessException e )
        {
            // Can not happen.
            throw new InternalError();
        }
        catch( InvocationTargetException e )
        {
            //TODO  What to do?
            e.printStackTrace();
            throw new InternalError();
        }
        return association;
    }
*/
/*

    private String getQualifiedPropertyName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }
*/
}