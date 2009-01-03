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
package org.qi4j.rest.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UnknownEntityTypeException;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.WriterRepresentation;

public class EntityResource extends Resource
{
    public static Object toValue( String newStringValue, String propertyName, String propertyType )
        throws IllegalArgumentException
    {
        Object newValue = null;
        try
        {
            // TODO A ton of more types need to be added here. Converter registration mechanism needed?
            newValue = null;
            if( propertyType.equals( String.class.getName() ) )
            {
                newValue = newStringValue;
            }
            else if( propertyType.equals( Integer.class.getName() ) )
            {
                newValue = Integer.parseInt( newStringValue );
            }
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Value '" + newStringValue + "' is not of type " + propertyType );
        }

        return newValue;
    }

    public static Object toString( Object newValue, String propertyName )
        throws IllegalArgumentException
    {
        if( newValue == null )
        {
            return "";
        }
        else
        {
            return newValue.toString();
        }
    }

    @Service private EntityStore entityStore;
    @Structure private Qi4jSPI spi;
    @Uses EntityStateSerializer entitySerializer;

    private QualifiedIdentity qualifiedIdentity;

    public EntityResource( @Uses Context context, @Uses Request request, @Uses Response response )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_HTML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_RDF_XML ) );
        getVariants().add( new Variant( MediaType.APPLICATION_JAVA_OBJECT ) );
        setModifiable( true );

        // /entity/{type}/{identity}
        Map<String, Object> attributes = getRequest().getAttributes();
        String identity = (String) attributes.get( "identity" );
        String type = (String) attributes.get( "type" );
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
        EntityState entityState = getEntityState();

        // Check modification date
        Date lastModified = getRequest().getConditions().getModifiedSince();
        if( lastModified != null )
        {
            if( lastModified.getTime() / 1000 == entityState.lastModified() / 1000 )
            {
                throw new ResourceException( Status.REDIRECTION_NOT_MODIFIED );
            }
        }

        // Generate the right representation according to its media type.
        if( MediaType.APPLICATION_RDF_XML.equals( variant.getMediaType() ) )
        {
            return entityHeaders( representRdfXml( entityState ), entityState );
        }
        else if( MediaType.TEXT_HTML.equals( variant.getMediaType() ) )
        {
            return entityHeaders( representHtml( entityState ), entityState );
        }
        else if( MediaType.APPLICATION_JAVA_OBJECT.equals( variant.getMediaType() ) )
        {
            return entityHeaders( representJava( entityState ), entityState );
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private EntityState getEntityState()
        throws ResourceException
    {
        EntityState entityState;
        try
        {
            entityState = entityStore.getEntityState( qualifiedIdentity );
        }
        catch( EntityNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        catch( UnknownEntityTypeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        return entityState;
    }

    private Representation entityHeaders( Representation representation, EntityState entityState )
    {
        representation.setModificationDate( new Date( entityState.lastModified() ) );
        representation.setTag( new Tag( "" + entityState.version() ) );
        representation.setCharacterSet( CharacterSet.UTF_8 );
        representation.setLanguages( Collections.singletonList( Language.ENGLISH ) );

        return representation;
    }

    private Representation representHtml( final EntityState entity )
    {
        return new WriterRepresentation( MediaType.TEXT_HTML )
        {
            public void write( Writer writer ) throws IOException
            {
                PrintWriter out = new PrintWriter( writer );
                EntityType entityType = entity.entityType();
                out.println( "<html><head><title>" + entity.qualifiedIdentity().identity() + "</title><link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" + entity.qualifiedIdentity().identity() + ".rdf\"/></head><body>" );
                out.println( "<h1>" + entity.qualifiedIdentity().identity() + "(<a href=\"../" + entity.qualifiedIdentity().type() + "\">" + entity.qualifiedIdentity().type() + "</a>)</h1>" );

                out.println( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
                out.println( "<fieldset><legend>Properties</legend>\n<table>" );
                for( PropertyType propertyType : entityType.properties() )
                {
                    Object value = entity.getProperty( propertyType.qualifiedName() );
                    if( value == null )
                    {
                        value = "";
                    }
                    out.println( "<tr><td>" +
                                 "<label for=\"" + propertyType.qualifiedName() + "\" >" +
                                 GenericPropertyInfo.getName( propertyType.qualifiedName() ) +
                                 "</label></td>\n" +
                                 "<td><input " +
                                 "type=\"text\" " +
                                 ( propertyType.propertyType() != PropertyType.PropertyTypeEnum.MUTABLE ? "readonly=\"true\" " : "" ) +
                                 "name=\"" + propertyType.qualifiedName() + "\" " +
                                 "value=\"" + EntityResource.toString( value, propertyType.qualifiedName() ) + "\"></td></tr>" );
                }
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>Associations</legend>\n<table>" );
                for( AssociationType associationType : entityType.associations() )
                {
                    Object value = entity.getAssociation( associationType.qualifiedName() );
                    if( value == null )
                    {
                        value = "";
                    }
                    out.println( "<tr><td>" +
                                 "<label for=\"" + associationType.qualifiedName() + "\" >" +
                                 GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                                 "</label></td>\n" +
                                 "<td><input " +
                                 "type=\"text\" " +
                                 "size=\"80\" " +
                                 "name=\"" + associationType.qualifiedName() + "\" " +
                                 "value=\"" + value + "\"></td></tr>" );
                }
                out.println( "</table></fieldset>\n" );

                out.println( "<fieldset><legend>Many associations</legend>\n<table>" );
                for( ManyAssociationType associationType : entityType.manyAssociations() )
                {
                    Collection<QualifiedIdentity> identities = entity.getManyAssociation( associationType.qualifiedName() );
                    String value = "";
                    for( QualifiedIdentity identity : identities )
                    {
                        value += identity.toString() + "\n";
                    }

                    out.println( "<tr><td>" +
                                 "<label for=\"" + associationType.qualifiedName() + "\" >" +
                                 GenericAssociationInfo.getName( associationType.qualifiedName() ) +
                                 "</label></td>\n" +
                                 "<td><textarea " +
                                 "rows=\"10\" " +
                                 "cols=\"80\" " +
                                 "name=\"" + associationType.qualifiedName() + "\" >" +
                                 value +
                                 "</textarea></td></tr>" );
                }
                out.println( "</table></fieldset>\n" );
                out.println( "<input type=\"submit\" value=\"Update\"/></form>\n" );

                out.println( "</body></html>\n" );
                out.close();
            }
        };
    }

    private Representation representRdfXml( final EntityState entity ) throws ResourceException
    {
        Representation representation = new WriterRepresentation( MediaType.APPLICATION_RDF_XML )
        {
            public void write( Writer writer ) throws IOException
            {
                try
                {
                    Iterable<Statement> statements = entitySerializer.serialize( entity );
                    new RdfXmlSerializer().serialize( statements, writer );
                }
                catch( RDFHandlerException e )
                {
                    throw (IOException) new IOException().initCause( e );
                }

                writer.close();
            }
        };
        representation.setCharacterSet( CharacterSet.UTF_8 );
        return representation;

    }

    private Representation representJava( final EntityState entity ) throws ResourceException
    {
        return new OutputRepresentation( MediaType.APPLICATION_JAVA_OBJECT )
        {
            public void write( OutputStream outputStream ) throws IOException
            {
                ObjectOutputStream oout = new ObjectOutputStream( outputStream );
                oout.writeObject( entity );
                oout.close();
            }
        };
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
        EntityState entity = getEntityState();

        Form form = new Form( entityRepresentation );

        try
        {
            for( PropertyType propertyType : entity.entityType().properties() )
            {
                if( propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE )
                {
                    String newStringValue = form.getFirstValue( propertyType.qualifiedName() );
                    Object newValue = toValue( newStringValue, propertyType.qualifiedName(), propertyType.type() );
                    entity.setProperty( propertyType.qualifiedName(), newValue );
                }
            }
            for( AssociationType associationType : entity.entityType().associations() )
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName() );
                if( newStringAssociation == null || newStringAssociation.equals( "" ) )
                {
                    entity.setAssociation( associationType.qualifiedName(), null );
                }
                else
                {
                    entity.setAssociation( associationType.qualifiedName(), QualifiedIdentity.parseQualifiedIdentity( newStringAssociation ) );
                }
            }
            for( ManyAssociationType associationType : entity.entityType().manyAssociations() )
            {
                String newStringAssociation = form.getFirstValue( associationType.qualifiedName() );
                Collection<QualifiedIdentity> manyAssociation = entity.getManyAssociation( associationType.qualifiedName() );
                if( newStringAssociation == null )
                {
                    continue;
                }

                manyAssociation.clear();
                BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                String qualifiedIdentity;
                try
                {
                    while( ( qualifiedIdentity = bufferedReader.readLine() ) != null )
                    {
                        manyAssociation.add( QualifiedIdentity.parseQualifiedIdentity( qualifiedIdentity ) );
                    }
                }
                catch( IOException e )
                {
                    // Ignore
                }
            }
        }
        catch( IllegalArgumentException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }

        try
        {
            entityStore.prepare( Collections.EMPTY_LIST, Collections.singleton( entity ), Collections.EMPTY_LIST ).commit();
        }
        catch( ConcurrentEntityStateModificationException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_CONFLICT );
        }
        catch( EntityNotFoundException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_GONE );
        }

        getResponse().setStatus( Status.SUCCESS_RESET_CONTENT );
    }

    @Override public boolean isModifiable()
    {
        return true;
    }
}