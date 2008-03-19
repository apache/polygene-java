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
import org.qi4j.association.AbstractAssociation;
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
    implements EntityStore<EntityStateInstance>
{
    private @ThisCompositeAs SerializationStore serializationStore;

    public EntityStateInstance newEntityState(
        UnitOfWork unitOfWork, String identity, CompositeBinding compositeBinding, Map<Method, Object> propertyValues ) throws StoreException
    {
        CompositeResolution compositeResolution = compositeBinding.getCompositeResolution();
        CompositeModel compositeModel = compositeResolution.getCompositeModel();
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) compositeModel.getCompositeClass();

        SerializedEntity id = new SerializedEntity( identity, compositeType );
        try
        {
            if( serializationStore.contains( id ) )
            {
                throw new EntityAlreadyExistsException( "Memory store", identity );
            }
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not verify if entity already exists", e );
        }

        EntityStateInstance entityStateInstance = createEntityState( EntityStatus.NEW, identity, compositeBinding, propertyValues );
        return entityStateInstance;
    }

    public EntityStateInstance getEntityState( UnitOfWork unitOfWork, String identity, CompositeBinding compositeBinding )
        throws StoreException
    {
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) compositeBinding.getCompositeResolution().getCompositeModel().getCompositeClass();
        SerializedEntity serializedEntity = new SerializedEntity( identity, compositeType );
        SerializedState serializedState = null;
        try
        {
            serializedState = serializationStore.get( serializedEntity, unitOfWork );
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not get serialized state", e );
        }

        if( serializedState == null )
        {
            throw new EntityNotFoundException( "Memory store", identity );
        }

        Map<Method, Object> propertyValues = new HashMap<Method, Object>();
        Map<String, Serializable> storedProperties = serializedState.getProperties();
        Iterable<PropertyBinding> propertyBindings = compositeBinding.getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            Object storedValue = storedProperties.get( propertyBinding.getQualifiedName() );
            propertyValues.put( propertyBinding.getPropertyResolution().getPropertyModel().getAccessor(), storedValue );
        }

        EntityStateInstance stateInstance = createEntityState( EntityStatus.LOADED, identity, compositeBinding, propertyValues );

        return stateInstance;
    }

    public StateCommitter prepare( UnitOfWork unitOfWork, Iterable<EntityStateInstance> states ) throws StoreException
    {
        final Map<SerializedEntity, SerializedState> newEntities = new HashMap<SerializedEntity, SerializedState>();
        final Map<SerializedEntity, SerializedState> updatedEntities = new HashMap<SerializedEntity, SerializedState>();
        final List<SerializedEntity> removedEntities = new ArrayList<SerializedEntity>();

        for( EntityStateInstance stateInstance : states )
        {
            if( stateInstance.getStatus() == EntityStatus.NEW || stateInstance.getStatus() == EntityStatus.LOADED )
            {
                Map<String, Serializable> serializedProperties = new HashMap<String, Serializable>();
                Map<String, SerializedEntity> serializedAssociations = new HashMap<String, SerializedEntity>();
                Map<String, Collection<SerializedEntity>> serializedManyAssociations = new HashMap<String, Collection<SerializedEntity>>();

                CompositeBinding binding = stateInstance.getCompositeBinding();
                Iterable<PropertyBinding> propertyBindings = binding.getPropertyBindings();
                for( PropertyBinding propertyBinding : propertyBindings )
                {
                    String qName = propertyBinding.getQualifiedName();
                    Method accessor = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor();
                    Property property = stateInstance.getProperty( accessor );
                    if( property instanceof PropertyInstance )
                    {
                        PropertyInstance propertyInstance = (PropertyInstance) stateInstance.getProperty( accessor );
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

                SerializedState state = new SerializedState( serializedProperties, serializedAssociations, serializedManyAssociations );
                Class<? extends Composite> compositeType = stateInstance.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
                SerializedEntity serializedEntity = new SerializedEntity( stateInstance.getIdentity(), (Class<? extends EntityComposite>) compositeType );

                if( stateInstance.getStatus() == EntityStatus.NEW )
                {
                    newEntities.put( serializedEntity, state );
                }
                else
                {
                    updatedEntities.put( serializedEntity, state );
                }
            }
            else
            {
                Class<? extends Composite> compositeType = stateInstance.getCompositeBinding().getCompositeResolution().getCompositeModel().getCompositeClass();
                SerializedEntity serializedEntity = new SerializedEntity( stateInstance.getIdentity(), (Class<? extends EntityComposite>) compositeType );
                removedEntities.add( serializedEntity );
            }
        }

        try
        {
            return serializationStore.prepare( newEntities, updatedEntities, removedEntities );
        }
        catch( IOException e )
        {
            throw new StoreException( "Could not prepare the underlying serialization store", e );
        }
    }

    private EntityStateInstance createEntityState( EntityStatus status, String identity, CompositeBinding compositeBinding, Map<Method, Object> propertyValues )
    {
        Map<Method, Property> properties = new HashMap<Method, Property>();
        Iterable<PropertyBinding> propertyBindings = compositeBinding.getPropertyBindings();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            PropertyResolution propertyResolution = propertyBinding.getPropertyResolution();
            PropertyModel propertyModel = propertyResolution.getPropertyModel();
            Method accessor = propertyModel.getAccessor();

            // Either use default value or the one that was set through the builder
            Object value = propertyBinding.getDefaultValue();
            if( propertyValues.containsKey( accessor ) )
            {
                value = propertyValues.get( accessor );
            }

            Class<?> type = accessor.getReturnType();
            if( ImmutableProperty.class.isAssignableFrom( type ) )
            {
                properties.put( accessor, new ImmutablePropertyInstance<Object>( propertyBinding, value ) );
            }
            else
            {
                properties.put( accessor, new PropertyInstance<Object>( propertyBinding, value ) );
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
