/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.serialization;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.Association;
import org.qi4j.association.ListAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.spi.association.AssociationBinding;
import org.qi4j.spi.association.AssociationInstance;
import org.qi4j.spi.association.ListAssociationInstance;
import org.qi4j.spi.association.SetAssociationInstance;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.PropertyModel;

public class SerializedEntityStoreMixin
    implements EntityStore
{
    private @ThisCompositeAs SerializationStore serializationStore;

    public EntityState newEntityState( String identity, CompositeBinding compositeBinding ) throws StoreException
    {
        CompositeResolution compositeResolution = compositeBinding.getCompositeResolution();
        CompositeModel compositeModel = compositeResolution.getCompositeModel();
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) compositeModel.getCompositeClass();

        EntityId id = new EntityId( identity, compositeType );
        try
        {
            if( serializationStore.contains( id ) )
            {
                throw new EntityAlreadyExistsException( "Serialization store", identity );
            }
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not verify if entity already exists", e );
        }

        EntityStateInstance entityStateInstance = createEntityState( EntityStatus.NEW, identity, compositeBinding );
        return entityStateInstance;
    }

    public EntityState getEntityState( UnitOfWork unitOfWork, String identity, CompositeBinding compositeBinding )
        throws StoreException
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass();
        EntityId entityId = new EntityId( identity, compositeType );
        SerializedState serializedState = null;
        try
        {
            serializedState = serializationStore.get( entityId, unitOfWork );
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not get serialized state", e );
        }

        if( serializedState == null )
        {
            throw new EntityNotFoundException( "Serialization store", identity );
        }

        EntityStateInstance stateInstance = createEntityState( EntityStatus.LOADED, identity, compositeBinding );

        // Populate properties with values
        Map<String, Serializable> storedProperties = serializedState.getProperties();
        Iterable<PropertyBinding> propertyBindings = compositeBinding.getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            Object storedValue = storedProperties.get( propertyBinding.getQualifiedName() );
            stateInstance.getProperty( propertyBinding.getPropertyResolution().getPropertyModel().getAccessor() ).set( storedValue );
        }

        // Populate associations
        Map<String, EntityId> storedAssociations = serializedState.getAssociations();
        Iterable<AssociationBinding> associationBindings = compositeBinding.getAssociationBindings();
        for( AssociationBinding associationBinding : associationBindings )
        {
            if( ManyAssociation.class.isAssignableFrom( associationBinding.getAssociationResolution().getAssociationModel().getAccessor().getReturnType() ) )
            {
                Collection<EntityId> assoc = serializedState.getManyAssociations().get( associationBinding.getQualifiedName() );
                ManyAssociation<Object> manyAssociation = (ManyAssociation<Object>) stateInstance.getAssociation( associationBinding.getAssociationResolution().getAssociationModel().getAccessor() );
                for( EntityId id : assoc )
                {
                    EntityComposite referencedComposite = unitOfWork.getReference( id.getIdentity(), id.getCompositeType() );
                    manyAssociation.add( referencedComposite );
                }
            }
            else
            {
                EntityId assoc = serializedState.getAssociations().get( associationBinding.getQualifiedName() );
                if( assoc != null )
                {
                    Association<Object> association = (Association<Object>) stateInstance.getAssociation( associationBinding.getAssociationResolution().getAssociationModel().getAccessor() );
                    EntityComposite referencedComposite = unitOfWork.getReference( assoc.getIdentity(), assoc.getCompositeType() );
                    association.set( referencedComposite );
                }
            }
        }

        return stateInstance;
    }

    public StateCommitter prepare( UnitOfWork unitOfWork, Iterable<EntityState> states ) throws StoreException
    {
        final Map<EntityId, SerializedState> newEntities = new HashMap<EntityId, SerializedState>();
        final Map<EntityId, SerializedState> updatedEntities = new HashMap<EntityId, SerializedState>();
        final List<EntityId> removedEntityIds = new ArrayList<EntityId>();

        for( EntityState stateInstance : states )
        {
            if( stateInstance.getStatus() == EntityStatus.NEW || stateInstance.getStatus() == EntityStatus.LOADED )
            {
                Map<String, Serializable> serializedProperties = new HashMap<String, Serializable>();
                Map<String, EntityId> serializedAssociations = new HashMap<String, EntityId>();
                Map<String, Collection<EntityId>> serializedManyAssociations = new HashMap<String, Collection<EntityId>>();

                CompositeBinding binding = stateInstance.getCompositeBinding();
                Iterable<PropertyBinding> propertyBindings = binding.getPropertyBindings();
                for( PropertyBinding propertyBinding : propertyBindings )
                {
                    String qName = propertyBinding.getQualifiedName();
                    Method accessor = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor();
                    Property property = stateInstance.getProperty( accessor );
                    if( property instanceof PropertyInstance )
                    {
                        PropertyInstance propertyInstance = (PropertyInstance) property;
                        Object value = propertyInstance.read();
                        serializedProperties.put( qName, (Serializable) value );
                    }
                    else if( property instanceof ImmutablePropertyInstance )
                    {
                        ImmutablePropertyInstance propertyInstance = (ImmutablePropertyInstance) stateInstance.getProperty( accessor );
                        Object value = propertyInstance.get();
                        serializedProperties.put( qName, (Serializable) value );
                    }
                }

                Iterable<AssociationBinding> associationBindings = binding.getAssociationBindings();
                for( AssociationBinding associationBinding : associationBindings )
                {
                    String qName = associationBinding.getQualifiedName();
                    Method accessor = associationBinding.getAssociationResolution().getAssociationModel().getAccessor();
                    AbstractAssociation association = stateInstance.getAssociation( accessor );
                    if( association instanceof AssociationInstance )
                    {
                        AssociationInstance AssociationInstance = (AssociationInstance) association;
                        EntityComposite value = (EntityComposite) AssociationInstance.read();
                        if( value != null )
                        {
                            EntityId entityIdId = new EntityId( value );
                            serializedAssociations.put( qName, entityIdId );
                        }
                    }
                    else if( association instanceof SetAssociationInstance )
                    {
                        SetAssociationInstance associationInstance = (SetAssociationInstance) association;
                        Set set = associationInstance.getAssociatedSet();
                        Set<EntityId> serializedSet = new HashSet<EntityId>( set.size() );
                        for( Object object : set )
                        {
                            EntityComposite entity = (EntityComposite) object;
                            EntityId entityId = new EntityId( entity );
                            serializedSet.add( entityId );

                        }
                        serializedManyAssociations.put( qName, serializedSet );
                    }
                    else if( association instanceof ListAssociationInstance )
                    {
                        ListAssociationInstance associationInstance = (ListAssociationInstance) association;
                        List list = associationInstance.getAssociatedList();
                        List<EntityId> serializedList = new ArrayList<EntityId>( list.size() );
                        for( Object object : list )
                        {
                            EntityComposite entity = (EntityComposite) object;
                            EntityId entityId = new EntityId( entity );
                            serializedList.add( entityId );

                        }
                        serializedManyAssociations.put( qName, serializedList );
                    }
                }

                SerializedState state = new SerializedState( serializedProperties, serializedAssociations, serializedManyAssociations );
                Class<? extends Composite> compositeType = stateInstance.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
                EntityId entityId = new EntityId( stateInstance.getIdentity(), (Class<? extends EntityComposite>) compositeType );

                if( stateInstance.getStatus() == EntityStatus.NEW )
                {
                    newEntities.put( entityId, state );
                }
                else
                {
                    updatedEntities.put( entityId, state );
                }
            }
            else
            {
                Class<? extends Composite> compositeType = stateInstance.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
                EntityId entityId = new EntityId( stateInstance.getIdentity(), (Class<? extends EntityComposite>) compositeType );
                removedEntityIds.add( entityId );
            }
        }

        try
        {
            return serializationStore.prepare( newEntities, updatedEntities, removedEntityIds );
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not prepare the underlying serialization store", e );
        }
    }

    private EntityStateInstance createEntityState( EntityStatus status, String identity, CompositeBinding compositeBinding )
    {
        Map<Method, Property> properties = new HashMap<Method, Property>();
        Iterable<PropertyBinding> propertyBindings = compositeBinding.getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            PropertyResolution propertyResolution = propertyBinding.getPropertyResolution();
            PropertyModel propertyModel = propertyResolution.getPropertyModel();
            Method accessor = propertyModel.getAccessor();

            Class<?> type = accessor.getReturnType();
            if( ImmutableProperty.class.isAssignableFrom( type ) )
            {
                properties.put( accessor, new ImmutablePropertyInstance<Object>( propertyBinding ) );
            }
            else
            {
                properties.put( accessor, new PropertyInstance<Object>( propertyBinding ) );
            }
        }

        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
        Iterable<AssociationBinding> associationBindings = compositeBinding.getAssociationBindings();
        for( AssociationBinding associationBinding : associationBindings )
        {
            AssociationResolution associationResolution = associationBinding.getAssociationResolution();
            AssociationModel associationModel = associationResolution.getAssociationModel();
            Method accessor = associationModel.getAccessor();
            Class<?> type = accessor.getReturnType();
            if( ListAssociation.class.isAssignableFrom( type ) )
            {
                ListAssociationInstance<Object> listInstance =
                    new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding );
                associations.put( accessor, listInstance );
            }
            else if( ManyAssociation.class.isAssignableFrom( type ) )
            {
                SetAssociationInstance setInstance = new SetAssociationInstance<Object>( new HashSet<Object>(), associationBinding );
                associations.put( accessor, setInstance );
            }
            else
            {
                AssociationInstance<Object> instance = new AssociationInstance<Object>( associationBinding, null );
                associations.put( accessor, instance );
            }
        }

        EntityStateInstance entityStateInstance = new EntityStateInstance( identity, compositeBinding, status, properties, associations );
        return entityStateInstance;
    }

}
