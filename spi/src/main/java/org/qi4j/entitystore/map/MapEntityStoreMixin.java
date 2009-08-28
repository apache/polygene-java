package org.qi4j.entitystore.map;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entity.helpers.EntityStoreSPI;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.util.json.JSONArray;
import org.qi4j.spi.util.json.JSONException;
import org.qi4j.spi.util.json.JSONObject;
import org.qi4j.spi.util.json.JSONTokener;
import org.qi4j.spi.util.json.JSONWriter;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore. Implement
 * MapEntityStore and add as mixin to the service using this mixin.
 * See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.
 */
public class MapEntityStoreMixin
    implements EntityStore, EntityStoreSPI, Activatable
{
    @This private MapEntityStore mapEntityStore;
    @This private EntityStoreSPI entityStoreSpi;

    protected String uuid;
    private int count;

    public void activate()
        throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";
    }

    public void passivate() throws Exception
    {
    }

    // EntityStore
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, ModuleSPI module )
    {
        return new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), module );
    }

    // EntityStoreSPI
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity,
                                       EntityDescriptor entityDescriptor )
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityDescriptor );
    }

    public synchronized EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity )
    {
        // Get state
        Reader in = mapEntityStore.get( identity );

        return readEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, in );
    }

    public StateCommitter apply( final Iterable<EntityState> state, final String version )
        throws EntityStoreException
    {
        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
                    {
                        public void visitMap( MapEntityStore.MapChanger changer ) throws IOException
                        {
                            for( EntityState entityState : state )
                            {
                                DefaultEntityState state = (DefaultEntityState) entityState;
                                if( state.status().equals( EntityStatus.NEW ) )
                                {
                                    Writer writer = changer.newEntity( state.identity(), state.entityDescriptor().entityType() );
                                    writeEntityState( state, writer, version );
                                    writer.close();
                                }
                                else if( state.status().equals( EntityStatus.UPDATED ) )
                                {
                                    Writer writer = changer.updateEntity( state.identity(), state.entityDescriptor().entityType() );
                                    writeEntityState( state, writer, version );
                                    writer.close();
                                }
                                else if( state.status().equals( EntityStatus.REMOVED ) )
                                {
                                    changer.removeEntity( state.identity(), state.entityDescriptor().entityType() );
                                }
                            }
                        }
                    } );
                }
                catch( IOException e )
                {
                    throw new EntityStoreException( e );
                }
            }

            public void cancel()
            {
            }
        };
    }

    public EntityStoreUnitOfWork visitEntityStates( final EntityStateVisitor visitor, ModuleSPI moduleInstance )
    {
        final DefaultEntityStoreUnitOfWork uow =
            new DefaultEntityStoreUnitOfWork( entityStoreSpi, newUnitOfWorkId(), moduleInstance );

        mapEntityStore.visitMap( new MapEntityStore.MapEntityStoreVisitor()
        {
            public void visitEntity( Reader entityState )
            {
                try
                {
                    EntityState entity = readEntityState( uow, entityState );
                    visitor.visitEntityState( entity );
                }
                catch( Exception e )
                {
                    Logger.getLogger( getClass().getName() ).throwing( getClass().getName(), "visitEntityStates", e );
                }
            }
        } );

        return uow;
    }

    protected String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    protected void writeEntityState( DefaultEntityState state, Writer writer, String identity )
        throws EntityStoreException
    {
        try
        {
            JSONWriter json = new JSONWriter( writer );
            JSONWriter properties = json.object().
                key( "identity" ).value( state.identity().identity() ).
                key( "type" ).value( state.entityDescriptor().entityType().type().name() ).
                key( "version" ).value( identity ).
                key( "modified" ).value( state.lastModified() ).
                key( "properties" ).object();
            EntityType entityType = state.entityDescriptor().entityType();
            for( PropertyType propertyType : entityType.properties() )
            {
                Object value = state.properties().get( propertyType.qualifiedName() );
                json.key( propertyType.qualifiedName().name() );
                if( value == null )
                {
                    json.value( null );
                }
                else
                {
                    propertyType.type().toJSON( value, json );
                }
            }

            JSONWriter associations = properties.endObject().key( "associations" ).object();
            for( Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : state.associations().entrySet() )
            {
                EntityReference value = stateNameEntityReferenceEntry.getValue();
                associations.key( stateNameEntityReferenceEntry.getKey().name() ).
                    value( value != null ? value.identity() : null );
            }

            JSONWriter manyAssociations = associations.endObject().key( "manyassociations" ).object();
            for( Map.Entry<QualifiedName, List<EntityReference>> stateNameListEntry : state.manyAssociations().entrySet() )
            {
                JSONWriter assocs = manyAssociations.key( stateNameListEntry.getKey().name() ).array();
                for( EntityReference entityReference : stateNameListEntry.getValue() )
                {
                    assocs.value( entityReference.identity() );
                }
                assocs.endArray();
            }
            manyAssociations.endObject().endObject();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( "Could not store EntityState", e );
        }
    }

    protected EntityState readEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
        throws EntityStoreException
    {
        try
        {
            ModuleSPI module = unitOfWork.module();
            JSONObject jsonObject = new JSONObject( new JSONTokener( entityState ) );
            String type = jsonObject.getString( "type" );
            EntityStatus status = EntityStatus.LOADED;

            EntityDescriptor entityDescriptor = module.entityDescriptor( type );
            if( entityDescriptor == null )
            {
                throw new EntityTypeNotFoundException( type );
            }

            Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();
            JSONObject props = jsonObject.getJSONObject( "properties" );
            for( PropertyDescriptor propertyDescriptor : entityDescriptor.state().properties() )
            {
                Object jsonValue;
                try
                {
                    jsonValue = props.get( propertyDescriptor.qualifiedName().name() );
                }
                catch( JSONException e )
                {
                    // Value not found, default it
                    Object initialValue = propertyDescriptor.initialValue();
                    properties.put( propertyDescriptor.qualifiedName(), initialValue );
                    status = EntityStatus.UPDATED;
                    continue;
                }
                if( jsonValue == JSONObject.NULL )
                {
                    properties.put( propertyDescriptor.qualifiedName(), null );
                }
                else
                {
                    Object value = ( (PropertyTypeDescriptor) propertyDescriptor ).propertyType().type().fromJSON( jsonValue, module );
                    properties.put( propertyDescriptor.qualifiedName(), value );
                }
            }

            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            JSONObject assocs = jsonObject.getJSONObject( "associations" );
            for( AssociationDescriptor associationType : entityDescriptor.state().associations() )
            {
                try
                {
                    Object jsonValue = assocs.get( associationType.qualifiedName().name() );
                    EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference( (String) jsonValue );
                    associations.put( associationType.qualifiedName(), value );
                }
                catch( JSONException e )
                {
                    // Association not found, default it to null
                    associations.put( associationType.qualifiedName(), null );
                    status = EntityStatus.UPDATED;
                }
            }

            JSONObject manyAssocs = jsonObject.getJSONObject( "manyassociations" );
            Map<QualifiedName, List<EntityReference>> manyAssociations = new HashMap<QualifiedName, List<EntityReference>>();
            for( ManyAssociationDescriptor manyAssociationType : entityDescriptor.state().manyAssociations() )
            {
                List<EntityReference> references = new ArrayList<EntityReference>();
                try
                {
                    JSONArray jsonValues = manyAssocs.getJSONArray( manyAssociationType.qualifiedName().name() );
                    for( int i = 0; i < jsonValues.length(); i++ )
                    {
                        Object jsonValue = jsonValues.getString( i );
                        EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference( (String) jsonValue );
                        references.add( value );
                    }
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
                catch( JSONException e )
                {
                    // ManyAssociation not found, default to empty one
                    manyAssociations.put( manyAssociationType.qualifiedName(), references );
                }
            }

            return new DefaultEntityState( unitOfWork,
                                           jsonObject.getString( "version" ),
                                           jsonObject.getLong( "modified" ),
                                           EntityReference.parseEntityReference( jsonObject.getString( "identity" ) ),
                                           status,
                                           entityDescriptor,
                                           properties,
                                           associations,
                                           manyAssociations
            );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }
}
