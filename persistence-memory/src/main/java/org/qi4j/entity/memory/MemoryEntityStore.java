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
package org.qi4j.entity.memory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ListAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
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
import org.qi4j.spi.entity.EntityStateInstance;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

public class MemoryEntityStore
    implements EntityStore<EntityStateInstance>
{
    private final Map<SerializedEntity, SerializedState> entityState;

    public MemoryEntityStore()
    {
        entityState = new ConcurrentHashMap<SerializedEntity, SerializedState>();
    }

    public EntityStateInstance newEntityState(
        EntitySession session, String identity, CompositeBinding compositeBinding, Map<Method, Object> propertyValues ) throws StoreException
    {
        CompositeResolution compositeResolution = compositeBinding.getCompositeResolution();
        CompositeModel compositeModel = compositeResolution.getCompositeModel();
        Class<? extends EntityComposite> compositeType = (Class<? extends EntityComposite>) compositeModel.getCompositeClass();

        SerializedEntity id = new SerializedEntity( identity, compositeType );
        if( entityState.containsKey( id ) )
        {
            throw new EntityAlreadyExistsException( "Memory store", identity );
        }

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

        EntityStateInstance entityStateInstance = new EntityStateInstance( identity, compositeBinding, EntityStatus.NEW, properties, associations );
        return entityStateInstance;
    }

    public EntityStateInstance getEntityState( EntitySession session, String identity, CompositeBinding compositeBinding )
        throws StoreException
    {
        // TODO
        return null;
    }

    public StateCommitter prepare( EntitySession session, Iterable<EntityStateInstance> states ) throws StoreException
    {
        for( EntityStateInstance stateInstance : states )
        {
            // TODO
        }

        return new StateCommitter()
        {
            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    private final boolean remove( SerializedEntity serializedEntity )
    {
        return entityState.remove( serializedEntity ) != null;
    }
}
