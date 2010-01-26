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

import org.restlet.resource.ServerResource;

public class EntityResource
    extends ServerResource
{
/*    public static Object toValue( String stringValue, QualifiedName propertyName, TypeName propertyType )
        throws IllegalArgumentException
    {
        Object newValue = null;
        try
        {
            // TODO A ton of more types need to be added here. Converter registration mechanism needed?
            newValue = null;
            if( propertyType.isClass( String.class ) )
            {
                newValue = stringValue;
            }
            else if( propertyType.isClass( Integer.class ) )
            {
                newValue = Integer.parseInt( stringValue );
            }
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Value '" + stringValue + "' is not of type " + propertyType );
        }

        return newValue;
    }

    public static Object toString( Object newValue )
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

    @Service private EntityTypeRegistry typeRegistry;

    @Structure private Qi4jSPI spi;

    @Uses EntityStateSerializer entitySerializer;

    private String identity;

    public EntityResource()
    {
        // Define the supported variant.
        getVariants().put( Method.ALL, Arrays.asList(
            MediaType.TEXT_HTML,
            MediaType.APPLICATION_RDF_XML,
            MediaType.APPLICATION_JAVA_OBJECT ) );
        setNegotiated( true );
    }

    @Override
    protected void doInit() throws ResourceException
    {
        // /entity/{identity}
        Map<String, Object> attributes = getRequest().getAttributes();
        identity = (String) attributes.get( "identity" );
    }

    @Override
    protected Representation delete() throws ResourceException
    {
        try
        {
            Usecase usecase = UsecaseBuilder.newUsecase( "Remove entity" );
            MetaInfo info = new MetaInfo();
            EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( usecase, info );
            EntityReference identityRef = EntityReference.parseEntityReference( identity );
            uow.getEntityState( identityRef ).remove();
            entityStore.apply( uow.identity(), uow.events(), usecase, info ).commit();
            getResponse().setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch( EntityNotFoundException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );
        }

        return new EmptyRepresentation();
    }

    @Override
    protected Representation get( Variant variant ) throws ResourceException
    {
        EntityStoreUnitOfWork uow = entityStore.newUnitOfWork( UsecaseBuilder.newUsecase( "Get entity" ), new MetaInfo() );
        EntityState entityState = getEntityState( uow );

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

    private EntityState getEntityState( EntityStoreUnitOfWork unitOfWork )
        throws ResourceException
    {
        EntityState entityState;
        try
        {
            EntityReference entityReference = EntityReference.parseEntityReference( identity );
            entityState = unitOfWork.getEntityState( entityReference );
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
                out.println( "<html><head><title>" + entity.identity() + "</title><link rel=\"alternate\" type=\"application/rdf+xml\" href=\"" + entity.identity() + ".rdf\"/></head><body>" );
                out.println( "<h1>" + entity.identity() + "</a>)</h1>" );

                out.println( "<form method=\"post\" action=\"" + getRequest().getResourceRef().getPath() + "\">\n" );
                out.println( "<fieldset><legend>Properties</legend>\n<table>" );

                for( EntityTypeReference entityTypeReference : entity.entityTypeReferences() )
                {
                    final EntityType type = typeRegistry.getEntityType( entityTypeReference );

                    for( PropertyType propertyType : type.properties() )
                    {
                        String value = entity.getProperty( propertyType.stateName() );
                        if( value == null )
                        {
                            value = "";
                        }
                        out.println( "<tr><td>" +
                                     "<label for=\"" + propertyType.stateName().qualifiedName() + "\" >" +
                                     propertyType.stateName().qualifiedName().name() +
                                     "</label></td>\n" +
                                     "<td><input " +
                                     "type=\"text\" " +
                                     ( propertyType.propertyType() != PropertyType.PropertyTypeEnum.MUTABLE ? "readonly=\"true\" " : "" ) +
                                     "name=\"" + propertyType.stateName().qualifiedName() + "\" " +
                                     "value=\"" + EntityResource.toString( value ) + "\"></td></tr>" );
                    }
                    out.println( "</table></fieldset>\n" );

                    out.println( "<fieldset><legend>Associations</legend>\n<table>" );
                    for( AssociationType associationType : type.associations() )
                    {
                        Object value = entity.getAssociation( associationType.stateName() );
                        if( value == null )
                        {
                            value = "";
                        }
                        out.println( "<tr><td>" +
                                     "<label for=\"" + associationType.qualifiedName() + "\" >" +
                                     associationType.qualifiedName().name() +
                                     "</label></td>\n" +
                                     "<td><input " +
                                     "type=\"text\" " +
                                     "size=\"80\" " +
                                     "name=\"" + associationType.qualifiedName() + "\" " +
                                     "value=\"" + value + "\"></td></tr>" );
                    }
                    out.println( "</table></fieldset>\n" );

                    out.println( "<fieldset><legend>Many manyAssociations</legend>\n<table>" );
                    for( ManyAssociationType associationType : type.manyAssociations() )
                    {
                        ManyAssociationState identities = entity.getManyAssociation( associationType.stateName() );
                        String value = "";
                        for( EntityReference identity : identities )
                        {
                            value += identity.toString() + "\n";
                        }

                        out.println( "<tr><td>" +
                                     "<label for=\"" + associationType.qualifiedName() + "\" >" +
                                     associationType.qualifiedName().name() +
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
                HashMap<StateName, String> properties = new HashMap<StateName, String>();
                HashMap<StateName, EntityReference> associations = new HashMap<StateName, EntityReference>();
                HashMap<StateName, List<EntityReference>> manyAssociations = new HashMap<StateName, List<EntityReference>>();
                for( EntityTypeReference entityTypeReference : entity.entityTypeReferences() )
                {
                    EntityType type = typeRegistry.getEntityType( entityTypeReference );
                    for( PropertyType propertyType : type.properties() )
                    {
                        properties.put( propertyType.stateName(), entity.getProperty( propertyType.stateName() ) );
                    }

                    for( AssociationType associationType : type.associations() )
                    {
                        associations.put( associationType.stateName(), entity.getAssociation( associationType.stateName() ) );
                    }

                    for( ManyAssociationType manyAssociationType : type.manyAssociations() )
                    {
                        ManyAssociationState state = entity.getManyAssociation( manyAssociationType.stateName() );
                        List<EntityReference> references = new ArrayList<EntityReference>( state.count() );
                        for( int i = 0; i < state.count(); i++ )
                        {
                            references.add( state.get( i ) );
                        }

                        manyAssociations.put( manyAssociationType.stateName(), references );
                    }
                }

                String entityVersion = entity.version();
                long lastModified = entity.lastModified();

                SerializableState state = new SerializableState( entity.identity(),
                                                                 entityVersion,
                                                                 lastModified,
                                                                 entity.entityTypeReferences(),
                                                                 properties,
                                                                 associations,
                                                                 manyAssociations );

                oout.writeUnshared( state );
                oout.close();
            }
        };
    }


    @Override
    protected Representation put( Representation representation, Variant variant ) throws ResourceException
    {
        return post( representation, variant );
    }

    *//**
 * Handle PUT requests.
 *//*
    @Override
    public Representation post( Representation entityRepresentation, Variant variant )
        throws ResourceException
    {
        Usecase usecase = UsecaseBuilder.newUsecase( "Update entity" );
        MetaInfo info = new MetaInfo();
        EntityStoreUnitOfWork unitOfWork = entityStore.newUnitOfWork( usecase, info );
        EntityState entity = getEntityState( unitOfWork );

        Form form = new Form( entityRepresentation );

        try
        {
            for( EntityTypeReference entityTypeReference : entity.entityTypeReferences() )
            {
                final EntityType type = typeRegistry.getEntityType( entityTypeReference );


                for( PropertyType propertyType : type.properties() )
                {
                    if( propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE )
                    {
                        String newStringValue = form.getFirstValue( propertyType.qualifiedName().toString() );
                        entity.setProperty( propertyType.stateName(), newStringValue );
                    }
                }
                for( AssociationType associationType : type.associations() )
                {
                    String newStringAssociation = form.getFirstValue( associationType.qualifiedName().toString() );
                    if( newStringAssociation == null || newStringAssociation.equals( "" ) )
                    {
                        entity.setAssociation( associationType.stateName(), null );
                    }
                    else
                    {
                        entity.setAssociation( associationType.stateName(), EntityReference.parseEntityReference( newStringAssociation ) );
                    }
                }
                for( ManyAssociationType associationType : type.manyAssociations() )
                {
                    String newStringAssociation = form.getFirstValue( associationType.qualifiedName().toString() );
                    ManyAssociationState manyAssociation = entity.getManyAssociation( associationType.stateName() );
                    if( newStringAssociation == null )
                    {
                        continue;
                    }

                    BufferedReader bufferedReader = new BufferedReader( new StringReader( newStringAssociation ) );
                    String identity;

                    try
                    {
                        // Synchronize old and new association
                        int index = 0;
                        while( ( identity = bufferedReader.readLine() ) != null )
                        {
                            EntityReference reference = new EntityReference( identity );

                            if( manyAssociation.count() < index && manyAssociation.get( index ).equals( reference ) )
                            {
                                continue;
                            }

                            manyAssociation.remove( reference );
                            manyAssociation.add( index++, reference );
                        }

                        // Remove "left-overs"
                        while( manyAssociation.count() > index )
                        {
                            manyAssociation.remove( manyAssociation.get( index + 1 ) );
                        }
                    }
                    catch( IOException e )
                    {
                        // Ignore
                    }
                }
            }
        }

        catch( IllegalArgumentException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }

        try
        {
            entityStore.apply( unitOfWork.identity(), unitOfWork.events(), usecase, info ).commit();
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

        return new EmptyRepresentation();
    }*/
}