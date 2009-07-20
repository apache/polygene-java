package org.qi4j.spi.entity.helpers;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.usecase.Usecase;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.spi.entity.*;
import org.qi4j.spi.serialization.FastObjectInputStream;
import org.qi4j.spi.serialization.FastObjectOutputStream;
import org.qi4j.spi.serialization.SerializableState;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;
import org.qi4j.spi.unitofwork.event.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Implementation of EntityStore that works with an implementation of MapEntityStore. Implement
 * MapEntityStore and add as mixin to the service using this mixin. See {@link org.qi4j.entitystore.memory.MemoryMapEntityStoreMixin} for reference.
 */
public class MapEntityStoreMixin
    implements EntityStore, EntityStoreEvents, UnitOfWorkEventFeed, Activatable
{
    private @This MapEntityStore mapEntityStore;
    private @This EntityStoreEvents events;

    protected String uuid;
    private int count;

    private ChangeEvent lastAppliedEvent;
    private ChangeEvent lastReadEvent;
    private ChangeEvent noneEvent = new ChangeEvent( "none", 0 );
    private EntityReference lastAppliedEventRef = new EntityReference( "lastAppliedEvent" );
    private EntityReference lastReadEventRef = new EntityReference( "lastReadEvent" );

    public void activate() throws Exception
    {
        uuid = UUID.randomUUID().toString() + "-";

        Usecase appliedUsecase = newUsecase( "Check for last applied event" );
        if( mapEntityStore.contains( lastAppliedEventRef, appliedUsecase, new MetaInfo() ) )
        {
            InputStream in = mapEntityStore.get( lastAppliedEventRef, appliedUsecase, new MetaInfo() );
            ObjectInputStream oin = new FastObjectInputStream( in, false );
            this.lastAppliedEvent = (ChangeEvent) oin.readObject();
        }
        else
        {
            lastAppliedEvent = noneEvent;
        }

        Usecase readUsecase = newUsecase( "Check for last read event" );
        if( mapEntityStore.contains( lastReadEventRef, readUsecase, new MetaInfo() ) )
        {
            InputStream in = mapEntityStore.get( lastAppliedEventRef, readUsecase, new MetaInfo() );
            ObjectInputStream oin = new FastObjectInputStream( in, false );
            this.lastReadEvent = (ChangeEvent) oin.readObject();
        }
        else
        {
            lastReadEvent = noneEvent;
        }
    }

    public void passivate() throws Exception
    {
    }

    // EntityStore
    public EntityStoreUnitOfWork newUnitOfWork( Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        return new DefaultEntityStoreUnitOfWork( this, newUnitOfWorkId(), usecaseMetaInfo, unitOfWorkMetaInfo );
    }

    public StateCommitter apply( final String identity, Iterable<UnitOfWorkEvent> events, final Usecase usecase, final MetaInfo metaInfo )
        throws EntityStoreException
    {
        // Store events
        final long timeStamp = System.currentTimeMillis();
        String previous = lastAppliedEvent.identity();
        final UnitOfWorkEventsEntry uowEventsEntry = new UnitOfWorkEventsEntry( identity, timeStamp, events, previous );

        try
        {
            mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
            {
                public void visitMap( MapEntityStore.MapChanger changer, Usecase usecase, MetaInfo unitOfWorkMetaInfo )
                {
                    try
                    {
                        OutputStream eventStream = changer.newEntity( new EntityReference( identity ) );
                        FastObjectOutputStream oout = new FastObjectOutputStream( eventStream, false );
                        oout.writeUnshared( uowEventsEntry );
                        oout.close();

                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException( e );
                    }
                }
            }, usecase, metaInfo );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Could not apply changes", e );
        }


        return new StateCommitter()
        {
            public void commit()
            {
                try
                {
                    mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
                    {
                        public void visitMap( MapEntityStore.MapChanger changer, Usecase usecase, MetaInfo unitOfWorkMetaInfo )
                            throws IOException
                        {
                            // Set last event marker
                            if( lastAppliedEvent.equals( noneEvent ) )
                            {
                                ChangeEvent newLastAppliedEvent = new ChangeEvent( identity, timeStamp );
                                OutputStream out = changer.newEntity( lastAppliedEventRef );
                                FastObjectOutputStream oout = new FastObjectOutputStream( out, false );
                                oout.writeUnshared( newLastAppliedEvent );
                                oout.close();
                                lastAppliedEvent = newLastAppliedEvent;
                            }
                            else
                            {
                                ChangeEvent newLastAppliedEvent = new ChangeEvent( identity, timeStamp );
                                OutputStream out = changer.updateEntity( lastAppliedEventRef );
                                FastObjectOutputStream oout = new FastObjectOutputStream( out, false );
                                oout.writeUnshared( newLastAppliedEvent );
                                oout.close();
                                lastAppliedEvent = newLastAppliedEvent;
                            }
                        }
                    }, usecase, metaInfo );
                }
                catch( IOException e )
                {
                    throw new EntityStoreException( "Could not commit changes", e );
                }
            }

            public void cancel()
            {
                try
                {
                    mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
                    {
                        public void visitMap( MapEntityStore.MapChanger changer, Usecase usecase, MetaInfo unitOfWorkMetaInfo ) throws IOException
                        {
                            // Remove events from store
                            changer.removeEntity( new EntityReference( identity ) );
                        }
                    }, usecase, metaInfo );
                }
                catch( IOException e )
                {
                    throw new EntityStoreException( "Could not cancel changes", e );
                }
            }
        };
    }

    // EntityStoreEvents
    public EntityState newEntityState( DefaultEntityStoreUnitOfWork unitOfWork, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        return new DefaultEntityState( unitOfWork, identity );
    }

    public synchronized EntityState getEntityState( DefaultEntityStoreUnitOfWork unitOfWork, EntityReference identity, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        // Bring state up to date first
        ensureUpToDate(usecaseMetaInfo, unitOfWorkMetaInfo);

        // Get state
        try
        {
            SerializableState serializableState = loadSerializableState( identity, usecaseMetaInfo, unitOfWorkMetaInfo );

            return getEntityState( unitOfWork, serializableState );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Could not get serialized state", e );
        }
    }

    private void apply( final UnitOfWorkEventsEntry event, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
        throws IOException, ClassNotFoundException
    {
        final Map<EntityReference, SerializableState> newStates = new HashMap<EntityReference, SerializableState>();
        final Map<EntityReference, SerializableState> updatedStates = new HashMap<EntityReference, SerializableState>();
        final Set<EntityReference> removedStates = new HashSet<EntityReference>();
        Map<EntityReference, SerializableState> states = new HashMap<EntityReference, SerializableState>();
        for( UnitOfWorkEvent unitOfWorkEvent : event.events() )
        {
            if( unitOfWorkEvent instanceof NewEntityEvent )
            {
                NewEntityEvent newEntityEvent = (NewEntityEvent) unitOfWorkEvent;
                SerializableState serializableState = new SerializableState( newEntityEvent.identity(),
                                                                             event.identity(),
                                                                             event.timeStamp(),
                                                                             new HashSet<EntityTypeReference>(),
                                                                             new HashMap<StateName, String>(),
                                                                             new HashMap<StateName, EntityReference>(),
                                                                             new HashMap<StateName, List<EntityReference>>() );
                newStates.put( newEntityEvent.identity(), serializableState );
                states.put( newEntityEvent.identity(), serializableState );
            }
            else if( unitOfWorkEvent instanceof RemoveEntityEvent )
            {
                RemoveEntityEvent removeEvent = (RemoveEntityEvent) unitOfWorkEvent;
                removedStates.add( removeEvent.identity() );
            }
            else if( unitOfWorkEvent instanceof SetPropertyEvent )
            {
                SetPropertyEvent setPropertyEvent = (SetPropertyEvent) unitOfWorkEvent;

                SerializableState serializableState = getState( updatedStates, states, setPropertyEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.setProperty( setPropertyEvent.stateName(), setPropertyEvent.value(), event.identity(), event.timeStamp() );
            }
            else if( unitOfWorkEvent instanceof SetAssociationEvent )
            {
                SetAssociationEvent setAssociationEvent = (SetAssociationEvent) unitOfWorkEvent;

                SerializableState serializableState = getState( updatedStates, states, setAssociationEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.setAssociation( setAssociationEvent.stateName(), setAssociationEvent.associatedEntity(), event.identity(), event.timeStamp() );
            }
            else if( unitOfWorkEvent instanceof AddManyAssociationEvent )
            {
                AddManyAssociationEvent addManyAssociationEvent = (AddManyAssociationEvent) unitOfWorkEvent;

                SerializableState serializableState = getState( updatedStates, states, addManyAssociationEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.addManyAssociation( addManyAssociationEvent.stateName(), addManyAssociationEvent.index(), addManyAssociationEvent.associatedEntity(), event.identity(), event.timeStamp() );
            }
            else if( unitOfWorkEvent instanceof RemoveManyAssociationEvent )
            {
                RemoveManyAssociationEvent removeManyAssociationEvent = (RemoveManyAssociationEvent) unitOfWorkEvent;

                SerializableState serializableState = getState( updatedStates, states, removeManyAssociationEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.removeManyAssociation( removeManyAssociationEvent.stateName(), removeManyAssociationEvent.associatedEntity(), event.identity(), event.timeStamp() );
            }
            else if( unitOfWorkEvent instanceof AddEntityTypeEvent )
            {
                AddEntityTypeEvent addEntityTypeEventEvent = (AddEntityTypeEvent) unitOfWorkEvent;
                SerializableState serializableState = getState( updatedStates, states, addEntityTypeEventEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.addEntityTypeReference( addEntityTypeEventEvent.entityType(), event.identity(), event.timeStamp() );
            }
            else if( unitOfWorkEvent instanceof RemoveEntityTypeEvent )
            {
                RemoveEntityTypeEvent removeEntityTypeEvent = (RemoveEntityTypeEvent) unitOfWorkEvent;
                SerializableState serializableState = getState( updatedStates, states, removeEntityTypeEvent.identity(), usecaseMetaInfo, unitOfWorkMetaInfo );
                serializableState.removeEntityTypeReference( removeEntityTypeEvent.entityType(), event.identity(), event.timeStamp() );
            }
        }

        final ChangeEvent newReadEvent = new ChangeEvent( event.identity(), event.timeStamp() );

        // Apply changes in store
        mapEntityStore.applyChanges( new MapEntityStore.MapChanges()
        {
            public void visitMap( MapEntityStore.MapChanger changer, Usecase usecase, MetaInfo unitOfWorkMetaInfo ) throws IOException
            {
                for( Map.Entry<EntityReference, SerializableState> entityReferenceSerializableStateEntry : newStates.entrySet() )
                {
                    OutputStream out = changer.newEntity( entityReferenceSerializableStateEntry.getKey() );
                    FastObjectOutputStream oout = new FastObjectOutputStream( out, false );
                    SerializableState state = entityReferenceSerializableStateEntry.getValue();
                    oout.writeUnshared( state );
                    oout.close();
                }

                for( Map.Entry<EntityReference, SerializableState> entityReferenceSerializableStateEntry : updatedStates.entrySet() )
                {
                    OutputStream out = changer.updateEntity( entityReferenceSerializableStateEntry.getKey() );
                    FastObjectOutputStream oout = new FastObjectOutputStream( out, false );
                    SerializableState state = entityReferenceSerializableStateEntry.getValue();
                    oout.writeUnshared( state );
                    oout.close();
                }

                for( EntityReference removedState : removedStates )
                {
                    changer.removeEntity( removedState );
                }

                // Marker
                OutputStream out;
                if( lastReadEvent.identity().equals( "none" ) )
                {
                    out = changer.newEntity( lastReadEventRef );
                }
                else
                {
                    out = changer.updateEntity( lastReadEventRef );
                }
                FastObjectOutputStream oout = new FastObjectOutputStream( out, false );
                oout.writeUnshared( newReadEvent );
                oout.close();
            }
        }, usecaseMetaInfo, unitOfWorkMetaInfo );

        lastReadEvent = newReadEvent;
    }

    private SerializableState getState( Map<EntityReference, SerializableState> updatedStates,
                                        Map<EntityReference, SerializableState> states,
                                        EntityReference reference,
                                        Usecase usecase, MetaInfo unitOfWork )
        throws IOException, ClassNotFoundException
    {
        SerializableState serializableState = states.get( reference );
        if( serializableState == null )
        {
            InputStream in = mapEntityStore.get( reference, usecase, unitOfWork );
            FastObjectInputStream oin = new FastObjectInputStream( in, false );
            serializableState = (SerializableState) oin.readUnshared();
            if( serializableState == null )
            {
                throw new IOException( "Serialized state was null" );
            }
            oin.close();
            updatedStates.put( reference, serializableState );
            states.put( reference, serializableState );
        }
        return serializableState;
    }

    private void ensureUpToDate(Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo)
    {
        // TODO Allow client to indicate that inconsistency is ok
        if( lastAppliedEvent != null )
        {
            if( lastReadEvent != null )
            {
                if( !lastAppliedEvent.equals( lastReadEvent ) )
                {
                    LinkedList<UnitOfWorkEventsEntry> events = new LinkedList<UnitOfWorkEventsEntry>();
                    String uowIdentity = lastAppliedEvent.identity();

                    try
                    {
                        while( !uowIdentity.equals( lastReadEvent.identity() ) )
                        {
                            InputStream in = mapEntityStore.get( new EntityReference( uowIdentity ), usecaseMetaInfo, unitOfWorkMetaInfo );
                            FastObjectInputStream oin = new FastObjectInputStream( in, false );
                            UnitOfWorkEventsEntry event = (UnitOfWorkEventsEntry) oin.readUnshared();
                            oin.close();
                            events.addFirst( event );
                            uowIdentity = event.previous();
                        }

                        // Apply events in reverse order
                        for( UnitOfWorkEventsEntry event : events )
                        {
                            apply( event, usecaseMetaInfo, unitOfWorkMetaInfo );
                        }
                    }
                    catch( IOException e )
                    {
                        throw new EntityStoreException( "Could not bring state up to date", e );
                    }
                    catch( ClassNotFoundException e )
                    {
                        throw new EntityStoreException( "Could not bring state up to date", e );
                    }
                }
            }
        }
    }

    public EntityStoreUnitOfWork visitEntityStates( final EntityStateVisitor visitor )
    {
        Usecase usecaseInfo = newUsecase("Visit entity state");
        MetaInfo uowMetaInfo = new MetaInfo();
        ensureUpToDate(usecaseInfo, uowMetaInfo);

        final DefaultEntityStoreUnitOfWork uow = new DefaultEntityStoreUnitOfWork( this, newUnitOfWorkId(), usecaseInfo, uowMetaInfo);
        mapEntityStore.visitMap( new MapEntityStore.MapEntityStoreVisitor()
        {
            public void visitEntity( InputStream entityState )
            {
                try
                {
                    ObjectInputStream oin = new FastObjectInputStream( entityState, false );
                    Object serializedObject = oin.readObject();
                    if (serializedObject instanceof SerializableState)
                    {
                        SerializableState state = (SerializableState) serializedObject;

                        EntityState entity = getEntityState( uow, state );
                        visitor.visitEntityState( entity );
                    }
                }
                catch( Exception e )
                {
                    Logger.getLogger( getClass().getName() ).throwing( getClass().getName(), "visitEntityStates", e );
                }
            }
        }, Usecase.DEFAULT, new MetaInfo() );

        return uow;
    }

    private String newUnitOfWorkId()
    {
        return uuid + Integer.toHexString( count++ );
    }

    private EntityState getEntityState( DefaultEntityStoreUnitOfWork unitOfWork, SerializableState serializableState )
    {
        EntityReference identity = serializableState.identity();
        Set<EntityTypeReference> entityTypeReferences = serializableState.entityTypeReferences();

        return new DefaultEntityState( unitOfWork,
                                       serializableState.version(),
                                       serializableState.lastModified(),
                                       identity,
                                       EntityStatus.LOADED,
                                       entityTypeReferences,
                                       serializableState.properties(),
                                       serializableState.associations(),
                                       serializableState.manyAssociations() );
    }

    private SerializableState loadSerializableState( EntityReference identity, Usecase usecase, MetaInfo unitOfWork )
        throws IOException, ClassNotFoundException, EntityNotFoundException
    {
        InputStream in = mapEntityStore.get( identity, usecase, unitOfWork );
        ObjectInputStream oin = new FastObjectInputStream( in, false );
        return (SerializableState) oin.readObject();
    }

    public Iterable<UnitOfWorkEventsEntry> getUnitOfWorkEvents( String startId, int count, Usecase usecaseMetaInfo, MetaInfo unitOfWorkMetaInfo )
    {
        if( startId == null )
        {
            startId = lastAppliedEvent.identity();
        }

        String uowIdentity = startId;

        LinkedList<UnitOfWorkEventsEntry> events = new LinkedList<UnitOfWorkEventsEntry>();
        try
        {
            int idx = 0;
            while( !uowIdentity.equals( "none" ) && idx < count )
            {
                InputStream in = mapEntityStore.get( new EntityReference( uowIdentity ), usecaseMetaInfo, unitOfWorkMetaInfo );
                FastObjectInputStream oin = new FastObjectInputStream( in, false );
                UnitOfWorkEventsEntry event = (UnitOfWorkEventsEntry) oin.readUnshared();
                oin.close();
                events.addFirst( event );
                uowIdentity = event.previous();
                idx++;
            }

            return events;
        }
        catch( IOException e )
        {
            throw new EntityStoreException( "Could not bring state up to date", e );
        }
        catch( ClassNotFoundException e )
        {
            throw new EntityStoreException( "Could not bring state up to date", e );
        }

    }
}
