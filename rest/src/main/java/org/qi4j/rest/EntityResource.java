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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.entity.association.Association;
import org.qi4j.property.Property;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.association.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.structure.Module;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EntityResource extends Resource
{
    @Structure private UnitOfWorkFactory unitOfWorkFactory;
    @Structure private Qi4jSPI spi;

    private String identity;
    private EntityComposite entity;
    private Class<? extends EntityComposite> compositeType;

    public EntityResource( @Uses Context context, @Uses Request request, @Uses Response response,
                           @Structure Module module )
        throws ClassNotFoundException
    {
        super( context, request, response );

        // Define the supported variant.
        getVariants().add( new Variant( MediaType.TEXT_XML ) );
        setModifiable( true );

        // Get the "itemName" attribute value taken from the URI template
        // /entity/{identity}.
        Map<String, Object> attributes = getRequest().getAttributes();
        this.identity = (String) attributes.get( "identity" );
        String type = (String) attributes.get( "type" );
        try
        {
            compositeType = module.findClass( type );
        }
        catch( ClassNotFoundException e )
        {
            // TODO Errorhandling
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }

    }

    /**
     * Handle DELETE requests.
     */
    @Override
    public void removeRepresentations()
        throws ResourceException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.currentUnitOfWork();
        entity = unitOfWork.getReference( identity, compositeType );
        if( entity != null )
        {
            unitOfWork.remove( entity );
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        else
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
            retrieveEntity();
        }
        // Generate the right representation according to its media type.
        if( MediaType.TEXT_XML.equals( variant.getMediaType() ) )
        {
            try
            {
                DomRepresentation representation = new DomRepresentation( MediaType.TEXT_XML );
                // Generate a DOM document representing the item.
                Document d = representation.getDocument();

                Element entityElement = d.createElement( "entity" );
                d.appendChild( entityElement );
                Element typeElement = d.createElement( "type" );
                entityElement.appendChild( typeElement );
                typeElement.appendChild( d.createTextNode( compositeType.getName() ) );
                Element identityElement = d.createElement( "identity" );
                identityElement.appendChild( d.createTextNode( entity.identity().get() ) );
                entityElement.appendChild( identityElement );
                Element propertiesElement = d.createElement( "properties" );
                entityElement.appendChild( propertiesElement );
                CompositeBinding binding = spi.getCompositeBinding( entity );
                for( PropertyBinding propertyBinding : binding.getPropertyBindings() )
                {
                    Property property = getProperty( propertyBinding );
                    Object value = property.get();
                    String name = property.name();
                    Element propertyElement = d.createElement( name );
                    if( value == null )
                    {
                        value = "[null]";
                    }
                    propertyElement.appendChild( d.createTextNode( value.toString() ) );
                    propertiesElement.appendChild( propertyElement );
                }
                Element associationsElement = null;
                for( AssociationBinding associationBinding : binding.getAssociationBindings() )
                {
                    final Association<?> association = getAssociation( associationBinding );
                    Object value = association.get();
                    if( value != null && !( value instanceof EntityComposite ) )
                    {
                        //TODO what to do in the case that association is not an entity composite
                        throw new InternalError( "Association is not an EntitityComposite" );
                    }
                    final EntityComposite entityComposite = (EntityComposite) value;
                    if( associationsElement == null )
                    {
                        associationsElement = d.createElement( "associations" );
                        entityElement.appendChild( associationsElement );
                    }
                    final Element associationElement = d.createElement( association.name() );
                    associationsElement.appendChild( associationElement );
                    if( entityComposite != null )
                    {
                        associationElement.setAttribute( "href", "/entity/" + entityComposite.type().getName() + "/" + entityComposite.identity().get() );
                        associationElement.appendChild( d.createTextNode( entityComposite.identity().get() ) );
                    }
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

    private void retrieveEntity()
    {
        // Get the item directly from the "persistence layer".
        UnitOfWork unitOfWork = unitOfWorkFactory.currentUnitOfWork();
        entity = unitOfWork.find( identity, compositeType );

        if( entity == null )
        {
            // This resource is not available.
            setAvailable( false );
        }
    }

    /**
     * Handle PUT requests.
     */
    @Override
    public void storeRepresentation( Representation entityRepresentation )
        throws ResourceException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.currentUnitOfWork();

        try
        {
            // Tells if the item is to be created of not.
            boolean creation = entityRepresentation == null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse( entityRepresentation.getStream() );
            Element rootElement = document.getDocumentElement();

            // The PUT request updates or creates the resource.
            if( creation )
            {
                entity = unitOfWork.newEntity( identity, compositeType );
            }
            else
            {
                entity = unitOfWork.find( identity, compositeType );
            }
            CompositeBinding binding = spi.getCompositeBinding( entity );
            Element properties = (Element) rootElement.getElementsByTagName( "properties" ).item( 0 );
            NodeList propertyNodes = properties.getChildNodes();
            for( int i = 0; i < propertyNodes.getLength(); i++ )
            {
                Node propertyNode = propertyNodes.item( i );
                String propertyName = propertyNode.getNodeName();
                // TODO: Handle Read/Only and no need to check for identity() explicitly
                if( !"identity".equals( propertyName ) )
                {
                    Method method = compositeType.getMethod( propertyName );
                    String name = getQualifiedPropertyName( method );
                    PropertyBinding propertyBinding = binding.getPropertyBinding( name );
                    PropertyResolution propertyResolution = propertyBinding.getPropertyResolution();
                    PropertyModel propertyModel = propertyResolution.getPropertyModel();
                    // TODO: Need handling of different types.
                    String propertyValue = propertyNode.getTextContent();
                    ( (Property) method.invoke( entity ) ).set( propertyValue );
                }
            }
            if( creation )
            {
                getResponse().setStatus( Status.SUCCESS_CREATED );
            }
            else
            {
                getResponse().setStatus( Status.SUCCESS_OK );
            }
        }
        catch( InvocationTargetException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Internal Error?", e );
        }
        catch( IllegalAccessException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Internal Error?", e );
        }
        catch( IOException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e );
        }
        catch( SAXException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid XML in request.", e );
        }
        catch( ParserConfigurationException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Internal Error?", e );
        }
        catch( NoSuchMethodException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e );
        }
    }

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

    private String getQualifiedPropertyName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }
}