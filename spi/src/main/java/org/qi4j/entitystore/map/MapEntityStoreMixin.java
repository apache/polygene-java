package org.qi4j.entitystore.map;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.helpers.DefaultEntityState;
import org.qi4j.spi.entity.helpers.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entity.helpers.EntityStoreSPI;
import org.qi4j.spi.entity.helpers.json.JSONException;
import org.qi4j.spi.entity.helpers.json.JSONObject;
import org.qi4j.spi.entity.helpers.json.JSONTokener;
import org.qi4j.spi.entity.helpers.json.JSONWriter;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore. Implement
 * MapEntityStore and add as mixin to the service using this mixin.
 * See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.
 */
public final class MapEntityStoreMixin
    implements EntityStore, EntityStoreSPI, Activatable
{
    private @This MapEntityStore mapEntityStore;
    private @This EntityStoreSPI entityStoreSpi;

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
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo, ModuleSPI module )
    {
        return new DefaultEntityStoreUnitOfWork(entityStoreSpi, newUnitOfWorkId(), module);
    }

    // EntityStoreSPI
    public EntityState newEntityState( EntityStoreUnitOfWork unitOfWork,
                                       EntityReference identity, EntityType entityType)
    {
        return new DefaultEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, identity, entityType );
    }

    public synchronized EntityState getEntityState( EntityStoreUnitOfWork unitOfWork, EntityReference identity)
    {
        // Get state
        Reader in = mapEntityStore.get( identity);

        return getEntityState( (DefaultEntityStoreUnitOfWork) unitOfWork, in );
    }

    public StateCommitter apply(final Iterable<EntityState> state, final String identity)
            throws EntityStoreException
    {
        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges(new MapEntityStore.MapChanges()
                    {
                        public void visitMap(MapEntityStore.MapChanger changer) throws IOException
                        {
                            try
                            {
                                for (EntityState entityState : state)
                                {
                                    DefaultEntityState state = (DefaultEntityState) entityState;
                                    if (state.status().equals(EntityStatus.NEW))
                                    {
                                        Writer writer = changer.newEntity(state.identity());
                                        writeEntityState(state, writer, identity);
                                        writer.close();
                                    } else if (state.status().equals(EntityStatus.UPDATED))
                                    {
                                        Writer writer = changer.updateEntity(state.identity());
                                        writeEntityState(state, writer, identity);
                                        writer.close();
                                    } else if (state.status().equals(EntityStatus.REMOVED))
                                    {
                                        changer.removeEntity(state.identity());
                                    }
                                }
                            } catch (JSONException e)
                            {
                                throw (IOException) new IOException().initCause(e);
                            }
                        }
                    });
                } catch (IOException e)
                {
                    throw new EntityStoreException(e);
                }
            }

            public void cancel()
            {
            }
        };
    }

    private void writeEntityState(DefaultEntityState state, Writer writer, String identity)
            throws JSONException
    {
        JSONWriter json = new JSONWriter(writer);
        JSONWriter properties = json.object().
                key("identity").value(state.identity().identity()).
                key("type").value(state.entityType().type().name()).
                key("version").value(identity).
                key("modified").value(state.lastModified()).
                key("properties").object();
        EntityType entityType = state.entityType();
        for (PropertyType propertyType : entityType.properties())
        {
            Object value = state.properties().get(propertyType.qualifiedName());
            json.key(propertyType.qualifiedName().name());
            propertyType.type().toJSON(value, json);
        }

        JSONWriter associations = properties.endObject().key("associations").object();
        for (Map.Entry<QualifiedName, EntityReference> stateNameEntityReferenceEntry : state.associations().entrySet())
        {
            EntityReference value = stateNameEntityReferenceEntry.getValue();
            associations.key(stateNameEntityReferenceEntry.getKey().name()).
                    value(value != null ? value.identity() : null);
        }

        JSONWriter manyAssociations = associations.endObject().key("manyassociations").object();
        for (Map.Entry<QualifiedName, List<EntityReference>> stateNameListEntry : state.manyAssociations().entrySet())
        {
            JSONWriter assocs = manyAssociations.key(stateNameListEntry.getKey().name()).array();
            for (EntityReference entityReference : stateNameListEntry.getValue())
            {
                assocs.value(entityReference.identity());
            }
            assocs.endArray();
        }
        manyAssociations.endObject().endObject();
    }

    public EntityStoreUnitOfWork visitEntityStates(final EntityStateVisitor visitor, ModuleSPI moduleInstance)
    {
        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork(entityStoreSpi, newUnitOfWorkId(), moduleInstance);

        mapEntityStore.visitMap( new MapEntityStore.MapEntityStoreVisitor()
        {
            public void visitEntity( Reader entityState )
            {
                try
                {
                    EntityState entity = getEntityState( uow, entityState );
                    visitor.visitEntityState( entity );
                }
                catch( Exception e )
                {
                    Logger.getLogger( getClass().getName() ).throwing( getClass().getName(), "visitEntityStates", e );
                }
            }
        });

        return uow;
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    private EntityState getEntityState( DefaultEntityStoreUnitOfWork unitOfWork, Reader entityState )
    {
        try
        {
            ModuleSPI module = unitOfWork.module();
            JSONObject jsonObject = new JSONObject(new JSONTokener(entityState));
            String type = jsonObject.getString("type");

            EntityType entityType = module.entityType(type);

            Map<QualifiedName, Object> properties = new HashMap<QualifiedName, Object>();
            JSONObject props = jsonObject.getJSONObject("properties");
            for (PropertyType propertyType : entityType.properties())
            {
                Object jsonValue = props.get(propertyType.qualifiedName().name());
                if (jsonValue == JSONObject.NULL)
                {
                    properties.put(propertyType.qualifiedName(), null);
                } else
                {
                    Object value = propertyType.type().fromJSON(jsonValue, module);
                    properties.put(propertyType.qualifiedName(), value);
                }
            }

            Map<QualifiedName, EntityReference> associations = new HashMap<QualifiedName, EntityReference>();
            JSONObject assocs = jsonObject.getJSONObject("associations");
            for (AssociationType associationType : entityType.associations())
            {
                Object jsonValue = assocs.get(associationType.qualifiedName().name());
                EntityReference value = jsonValue == JSONObject.NULL ? null: EntityReference.parseEntityReference((String) jsonValue);
                associations.put(associationType.qualifiedName(), value);
            }

            return new DefaultEntityState(unitOfWork,
                    jsonObject.getString("version"),
                    jsonObject.getLong("modified"),
                    EntityReference.parseEntityReference(jsonObject.getString("identity")),
                    EntityStatus.LOADED,
                    entityType,
                    properties,
                    associations,
                    new HashMap<QualifiedName, List<EntityReference>>() // TODO!!!!
                    );
        } catch (JSONException e)
        {
            throw new EntityStoreException(e);
        }
    }
}
