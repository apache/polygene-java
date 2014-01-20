/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.rest.server.api;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.server.restlet.ConstraintViolationMessages;
import org.qi4j.library.rest.server.restlet.InteractionConstraints;
import org.qi4j.library.rest.server.restlet.RequestReaderDelegator;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.spi.ResultConverter;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;
import static org.qi4j.functional.Iterables.iterable;
import static org.qi4j.library.rest.server.api.ObjectSelection.current;

/**
 * JAVADOC
 */
public class ContextResource
    implements Uniform
{
    private static final String ARGUMENTS = "arguments";
    public static final String RESOURCE_VALIDITY = "validity";

    // API fields
    @Structure
    protected Module module;

    // Private state
    private final Map<String, Method> resourceMethodQueries = new HashMap<>();
    private final Map<String, Method> resourceMethodCommands = new HashMap<>();
    private final Map<String, Method> subResources = new LinkedHashMap<>();
    private final List<Method> resourceQueries = new ArrayList<>();
    private final List<Method> resourceCommands = new ArrayList<>();

    @Structure
    private Qi4jSPI spi;

    @Service
    private ResponseWriterDelegator responseWriter;

    @Service
    private RequestReaderDelegator requestReader;

    @Service
    private InteractionConstraints constraints;

    @Optional
    @Service
    private ResultConverter converter;

    @Uses
    private ContextRestlet restlet;

    public ContextResource()
    {
        // Resource method mappings
        for( Method method : getClass().getMethods() )
        {
            if( ContextResource.class.isAssignableFrom( method.getDeclaringClass() )
                && !ContextResource.class.equals( method.getDeclaringClass() )
                && !method.isSynthetic() )
            {
                if( method.getAnnotation( SubResource.class ) == null )
                {
                    Method oldMethod;

                    if( isCommand( method ) )
                    {
                        oldMethod = resourceMethodCommands.put( method.getName().toLowerCase(), method );
                        resourceCommands.add( method );
                    }
                    else
                    {
                        oldMethod = resourceMethodQueries.put( method.getName().toLowerCase(), method );
                        resourceQueries.add( method );
                    }

                    if( oldMethod != null )
                    {
                        throw new IllegalStateException( "Two methods in resource " + getClass().getName() + " with same name " + oldMethod
                            .getName() + ", which is not allowed" );
                    }
                }
                else
                {
                    Method oldMethod = subResources.put( method.getName().toLowerCase(), method );

                    if( oldMethod != null )
                    {
                        throw new IllegalStateException( "Two methods in resource " + getClass().getName() + " with same name " + oldMethod
                            .getName() + ", which is not allowed" );
                    }
                }
            }
        }
    }

    // Uniform implementation
    @Override
    public final void handle( Request request, Response response )
    {
        ObjectSelection objectSelection = current();

        // Check constraints for this resource
        if( !constraints.isValid( getClass(), objectSelection, module ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN );
        }

        // Find remaining segments
        List<String> segments = getSegments();

        if( segments.size() > 0 )
        {
            String segment = segments.remove( 0 );

            if( segments.size() > 0 )
            {
                handleSubResource( segment );
            }
            else
            {
                handleResource( segment );
            }
        }
    }

    // API methods
    protected void setResourceValidity( EntityComposite entity )
    {
        Request request = Request.getCurrent();
        ResourceValidity validity = new ResourceValidity( entity, spi, request );
        request.getAttributes().put( RESOURCE_VALIDITY, validity );
    }

    protected void subResource( Class<? extends ContextResource> subResourceClass )
    {
        restlet.subResource( subResourceClass );
    }

    protected <T> T select( Class<T> entityClass, String id )
        throws ResourceException
    {
        try
        {
            T composite = module.currentUnitOfWork().get( entityClass, id );
            current().select( composite );
            return composite;
        }
        catch( EntityTypeNotFoundException | NoSuchEntityException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
    }

    protected <T> T selectFromManyAssociation( ManyAssociation<T> manyAssociation, String id )
        throws ResourceException
    {
        T entity = (T) module.currentUnitOfWork().get( Object.class, id );
        if( !manyAssociation.contains( entity ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        current().select( entity );
        return entity;
    }
    
    protected <T> T selectFromNamedAssociation( NamedAssociation<T> namedAssociation, String id )
        throws ResourceException
    {
        T entity = (T) module.currentUnitOfWork().get( Object.class, id );
        String name = namedAssociation.nameOf( entity );
        if(name == null)
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        current().select( entity );
        return entity;
    }

    protected void selectFromList( List<?> list, String indexString )
    {
        Integer index = Integer.decode( indexString );

        if( index < 0 || index >= list.size() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        current().select( index );

        Object value = list.get( index );
        current().select( value );
    }

    protected Locale getLocale()
    {
        Request request = Request.getCurrent();

        List<Preference<Language>> preferenceList = request.getClientInfo().getAcceptedLanguages();

        if( preferenceList.isEmpty() )
        {
            return Locale.getDefault();
        }

        Language language = preferenceList
            .get( 0 ).getMetadata();
        String[] localeStr = language.getName().split( "-" );

        Locale locale;
        switch( localeStr.length )
        {
        case 1:
            locale = new Locale( localeStr[ 0 ] );
            break;
        case 2:
            locale = new Locale( localeStr[ 0 ], localeStr[ 1 ] );
            break;
        case 3:
            locale = new Locale( localeStr[ 0 ], localeStr[ 1 ], localeStr[ 2 ] );
            break;
        default:
            locale = Locale.getDefault();
        }
        return locale;
    }

    protected <T> T context( Class<T> contextClass )
    {
        return module.newObject( contextClass, ObjectSelection.current().toArray() );
    }

    // Private implementation
    private void handleSubResource( String segment )
    {
        if( this instanceof SubResources )
        {
            SubResources subResources = (SubResources) this;
            try
            {
                StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get( "template" );
                template.append( "resource/" );
                subResources.resource( URLDecoder.decode( segment, "UTF-8" ) );
            }
            catch( UnsupportedEncodingException e )
            {
                subResources.resource( segment );
            }
        }
        else
        {
            // Find @SubResource annotated method
            try
            {
                Method method = getSubResourceMethod( segment );

                StringBuilder template = (StringBuilder) Request.getCurrent().getAttributes().get( "template" );
                template.append( segment ).append( "/" );

                method.invoke( this );
            }
            catch( Throwable e )
            {
                handleException( Response.getCurrent(), e );
            }
        }
    }

    private Method getSubResourceMethod( String resourceName )
        throws ResourceException
    {
        Method method = subResources.get( resourceName );
        if( method != null )
        {
            return method;
        }

        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
    }

    private void resource()
    {
        Request request = Request.getCurrent();
        Response response = Response.getCurrent();
        if( !request.getMethod().equals( org.restlet.data.Method.GET ) )
        {
            response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
            return;
        }

        ObjectSelection objectSelection = current();

        // Check for interaction->method mappings
        if( ResourceDelete.class.isAssignableFrom( getClass() ) )
        {
            response.getAllowedMethods().add( org.restlet.data.Method.DELETE );
        }
        if( ResourceUpdate.class.isAssignableFrom( getClass() ) )
        {
            response.getAllowedMethods().add( org.restlet.data.Method.PUT );
        }

        // Construct resource
        ValueBuilder<Resource> builder = module.newValueBuilder( Resource.class );

        List<Link> queriesProperty = builder.prototype().queries().get();
        for( Method query : resourceQueries )
        {
            if( constraints.isValid( query, objectSelection, module ) )
            {
                ValueBuilder<Link> linkBuilder = module.newValueBuilder( Link.class );
                Link prototype = linkBuilder.prototype();
                prototype.classes().set( "query" );
                prototype.text().set( humanReadable( query.getName() ) );
                prototype.href().set( query.getName().toLowerCase() );
                prototype.rel().set( query.getName().toLowerCase() );
                prototype.id().set( query.getName().toLowerCase() );
                queriesProperty.add( linkBuilder.newInstance() );
            }
        }

        List<Link> commandsProperty = builder.prototype().commands().get();
        for( Method command : resourceCommands )
        {
            if( constraints.isValid( command, objectSelection, module ) )
            {
                ValueBuilder<Link> linkBuilder = module.newValueBuilder( Link.class );
                Link prototype = linkBuilder.prototype();
                prototype.classes().set( "command" );
                prototype.text().set( humanReadable( command.getName() ) );
                prototype.href().set( command.getName().toLowerCase() );
                prototype.rel().set( command.getName().toLowerCase() );
                prototype.id().set( command.getName().toLowerCase() );
                commandsProperty.add( linkBuilder.newInstance() );
            }
        }

        List<Link> resourcesProperty = builder.prototype().resources().get();
        for( Method subResource : subResources.values() )
        {
            if( constraints.isValid( subResource, objectSelection, module ) )
            {
                ValueBuilder<Link> linkBuilder = module.newValueBuilder( Link.class );
                Link prototype = linkBuilder.prototype();
                prototype.classes().set( "resource" );
                prototype.text().set( humanReadable( subResource.getName() ) );
                prototype.href().set( subResource.getName().toLowerCase() + "/" );
                prototype.rel().set( subResource.getName().toLowerCase() );
                prototype.id().set( subResource.getName().toLowerCase() );
                resourcesProperty.add( linkBuilder.newInstance() );
            }
        }

        try
        {
            Method indexMethod = resourceMethodQueries.get( "index" );
            if( indexMethod != null )
            {
                Object index = convert( indexMethod.invoke( this ) );

                if( index != null && index instanceof ValueComposite )
                {
                    builder.prototype().index().set( (ValueComposite) index );
                }
            }
        }
        catch( Throwable e )
        {
            // Ignore
        }

        try
        {
            responseWriter.writeResponse( builder.newInstance(), response );
        }
        catch( Throwable e )
        {
            handleException( response, e );
        }
    }

    private boolean isCommand( Method method )
    {
        return method.getReturnType().equals( Void.TYPE ) || method.getName().equals( "create" );
    }

    /**
     * Transform a Java name to a human readable string by replacing uppercase characters
     * with space+toLowerCase(char)
     * Example:
     * changeDescription -> Change description
     * doStuffNow -> Do stuff now
     *
     * @param name
     *
     * @return
     */
    private String humanReadable( String name )
    {
        StringBuilder humanReadableString = new StringBuilder();

        for( int i = 0; i < name.length(); i++ )
        {
            char character = name.charAt( i );
            if( i == 0 )
            {
                // Capitalize first character
                humanReadableString.append( Character.toUpperCase( character ) );
            }
            else if( Character.isLowerCase( character ) )
            {
                humanReadableString.append( character );
            }
            else
            {
                humanReadableString.append( ' ' ).append( Character.toLowerCase( character ) );
            }
        }

        return humanReadableString.toString();
    }

    private void result( Object resultValue )
        throws Exception
    {
        if( resultValue != null )
        {
            if( !responseWriter.writeResponse( resultValue, Response.getCurrent() ) )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "No result writer for type " + resultValue.getClass()
                    .getName() );
            }
        }
    }

    private List<String> getSegments()
    {
        return (List<String>) Request.getCurrent().getAttributes().get( "segments" );
    }

    private void handleResource( String segment )
    {
        Request request = Request.getCurrent();
        if( segment.isEmpty() || segment.equals( "." ) )
        {
            StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
            template.append( "resource" );

            // Index for this resource
            resource();
        }
        else
        {
            StringBuilder template = (StringBuilder) request.getAttributes().get( "template" );
            template.append( segment );

            if( resourceMethodCommands.containsKey( segment ) )
            {
                handleCommand( segment );
            }
            else
            {
                handleQuery( segment );
            }
        }
    }

    private void handleCommand( String segment )
    {
        Request request = Request.getCurrent();
        Response response = Response.getCurrent();

        // Check if this is a request to show the form for this command
        Method interactionMethod = resourceMethodCommands.get( segment );
        if( shouldShowCommandForm( interactionMethod ) )
        {
            // Show form

            // TODO This should check if method is idempotent
            response.getAllowedMethods().add( org.restlet.data.Method.POST );

            try
            {
                // Check if there is a query with this name - if so invoke it
                Method queryMethod = resourceMethodQueries.get( segment );
                if( queryMethod != null )
                {
                    result( queryMethod.invoke( this ) );
                }
                else
                {
                    request.setMethod( org.restlet.data.Method.POST );
                    response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
                    result( formForMethod( interactionMethod ) );
                }
            }
            catch( Exception e )
            {
                handleException( response, e );
            }
        }
        else
        {
            // Check timestamps
            ResourceValidity validity = (ResourceValidity) request.getAttributes().get( RESOURCE_VALIDITY );
            if( validity != null )
            {
                validity.checkRequest();
            }

            // We have input data - do command
            // Check method constraints
            if( !constraints.isValid( interactionMethod, current(), module ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }

            // Create argument
            Object[] arguments = requestReader.readRequest( Request.getCurrent(), interactionMethod );
            Request.getCurrent().getAttributes().put( ARGUMENTS, arguments );

            // Invoke method
            try
            {
                Object result = interactionMethod.invoke( this, arguments );

                if( result != null )
                {
                    if( result instanceof Representation )
                    {
                        response.setEntity( (Representation) result );
                    }
                    else
                    {
                        result( convert( result ) );
                    }
                }
            }
            catch( Throwable e )
            {
                handleException( response, e );
            }
        }
    }

    private boolean shouldShowCommandForm( Method interactionMethod )
    {
        // Show form on GET/HEAD
        if( Request.getCurrent().getMethod().isSafe() )
        {
            return true;
        }

        if( interactionMethod.getParameterTypes().length > 0 )
        {
            return !( interactionMethod.getParameterTypes()[ 0 ].equals( Response.class ) || Request.getCurrent()
                .getEntity()
                .isAvailable() || Request.getCurrent().getEntityAsText() != null || Request.getCurrent()
                                                                                        .getResourceRef()
                                                                                        .getQuery() != null );
        }

        return false;
    }

    private void handleQuery( String segment )
    {
        Request request = Request.getCurrent();
        Response response = Response.getCurrent();

        // Query

        // Try to locate either the query method or command method that should be used
        Method queryMethod = resourceMethodQueries.get( segment );
        if( queryMethod == null )
        {
            queryMethod = resourceMethodCommands.get( segment );
        }

        if( queryMethod == null )
        {
            // Not found as interaction, try SubResource
            Method resourceMethod = subResources.get( segment );
            if( resourceMethod != null && resourceMethod.getAnnotation( SubResource.class ) != null )
            {
                // Found it! Redirect to it
                response.setStatus( Status.REDIRECTION_FOUND );
                response
                    .setLocationRef( new Reference( request
                                                        .getResourceRef()
                                                        .toString() + "/" ).toString() );
                return;
            }
            else
            {
                // 404
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }
        }

        // Check if this is a request to show the form for this interaction
        if( ( request
                  .getMethod()
                  .isSafe() && queryMethod.getParameterTypes().length != 0 && request
                                                                                  .getResourceRef()
                                                                                  .getQuery() == null ) ||
            ( !request
                .getMethod()
                .isSafe() && queryMethod.getParameterTypes().length != 0 && !( request
                                                                                   .getEntity()
                                                                                   .isAvailable() || request
                                                                                                         .getResourceRef()
                                                                                                         .getQuery() != null || queryMethod
                .getParameterTypes()[ 0 ].equals( Response.class ) ) ) )
        {
            // Show form
            try
            {
                // Tell client to try again
                response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
                response.getAllowedMethods().add( org.restlet.data.Method.GET );
                response.getAllowedMethods().add( org.restlet.data.Method.POST );
                result( formForMethod( queryMethod ) );
            }
            catch( Exception e )
            {
                handleException( response, e );
            }
        }
        else
        {
            // Check timestamps
            ResourceValidity validity = (ResourceValidity) request.getAttributes().get( RESOURCE_VALIDITY );
            if( validity != null )
            {
                validity.checkRequest();
            }

            // We have input data - do query
            // Check method constraints
            if( !constraints.isValid( queryMethod, current(), module ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
            }

            try
            {
                // Create argument
                Object[] arguments;
                if( queryMethod.getParameterTypes().length > 0 )
                {
                    try
                    {
                        arguments = requestReader.readRequest( Request.getCurrent(), queryMethod );

                        if( arguments == null )
                        {
                            // Show form
                            result( formForMethod( queryMethod ) );
                            return;
                        }
                    }
                    catch( IllegalArgumentException e )
                    {
                        // Still missing some values - show form
                        result( formForMethod( queryMethod ) );
                        return;
                    }
                }
                else
                {
                    // No arguments to this query
                    arguments = new Object[ 0 ];
                }

                // Invoke method
                Request.getCurrent().getAttributes().put( ARGUMENTS, arguments );
                Object result = queryMethod.invoke( this, arguments );
                if( result != null )
                {
                    if( result instanceof Representation )
                    {
                        response.setEntity( (Representation) result );
                    }
                    else
                    {
                        result( convert( result ) );
                    }
                }
            }
            catch( Throwable e )
            {
                handleException( response, e );
            }
        }
    }

    private Object convert( Object result )
    {
        if( converter != null )
        {
            result = converter.convert( result, Request.getCurrent(), (Object[]) Request.getCurrent()
                .getAttributes()
                .get( ARGUMENTS ) );
        }

        return result;
    }

    private void handleException( Response response, Throwable ex )
    {
        while( ex instanceof InvocationTargetException )
        {
            ex = ex.getCause();
        }

        try
        {
            throw ex;
        }
        catch( ResourceException e )
        {
            // IAE (or subclasses) are considered client faults
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( e.getStatus() );
        }
        catch( ConstraintViolationException e )
        {
            try
            {
                ConstraintViolationMessages cvm = new ConstraintViolationMessages();

                // CVE are considered client faults
                String messages = "";
                Locale locale = ObjectSelection.type( Locale.class );
                for( ConstraintViolation constraintViolation : e.constraintViolations() )
                {
                    if( !messages.isEmpty() )
                    {
                        messages += "\n";
                    }
                    messages += cvm.getMessage( constraintViolation, locale );
                }

                response.setEntity( new StringRepresentation( messages ) );
                response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
            }
            catch( Exception e1 )
            {
                response.setEntity( new StringRepresentation( e.getMessage() ) );
                response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
            }
        }
        catch( IllegalArgumentException e )
        {
            // IAE (or subclasses) are considered client faults
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
        }
        catch( RuntimeException e )
        {
            // RuntimeExceptions are considered server faults
            LoggerFactory.getLogger( getClass() ).warn( "Exception thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
        }
        catch( Exception e )
        {
            // Checked exceptions are considered client faults
            String s = e.getMessage();
            if( s == null )
            {
                s = e.getClass().getSimpleName();
            }
            response.setEntity( new StringRepresentation( s ) );
            response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
        }
        catch( Throwable e )
        {
            // Anything else are considered server faults
            LoggerFactory.getLogger( getClass() ).error( "Exception thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
        }
    }

    private Form formForMethod( Method interactionMethod )
    {
        Form form = new Form();

        Form queryAsForm = Request.getCurrent().getResourceRef().getQueryAsForm();
        Form entityAsForm;
        Representation representation = Request.getCurrent().getEntity();
        if( representation != null && !EmptyRepresentation.class.isInstance( representation ) )
        {
            entityAsForm = new Form( representation );
        }
        else
        {
            entityAsForm = new Form();
        }

        Class<?> valueType = interactionMethod.getParameterTypes()[ 0 ];
        if( ValueComposite.class.isAssignableFrom( valueType ) )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( valueType.getName() );

            for( PropertyDescriptor propertyDescriptor : valueDescriptor.state().properties() )
            {
                String value = getValue( propertyDescriptor.qualifiedName().name(), queryAsForm, entityAsForm );

                if( value == null )
                {
                    Object initialValue = propertyDescriptor.initialValue( module );
                    if( initialValue != null )
                    {
                        value = initialValue.toString();
                    }
                }

                form.add( propertyDescriptor.qualifiedName().name(), value );
            }
        }
        else if( valueType.isInterface() && interactionMethod.getParameterTypes().length == 1 )
        {
            // Single entity as input
            form.add( "entity", getValue( "entity", queryAsForm, entityAsForm ) );
        }
        else
        {
            // Construct form out of individual parameters instead
            for( Annotation[] annotations : interactionMethod.getParameterAnnotations() )
            {
                Name name = (Name) first( filter( isType( Name.class ), iterable( annotations ) ) );
                form.add( name.value(), getValue( name.value(), queryAsForm, entityAsForm ) );
            }
        }

        return form;
    }

    private String getValue( String name, Form queryAsForm, Form entityAsForm )
    {
        String value = queryAsForm.getFirstValue( name );
        if( value == null )
        {
            value = entityAsForm.getFirstValue( name );
        }
        return value;
    }
}
