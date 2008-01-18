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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.association.ManyAssociation;
import org.qi4j.association.SetAssociation;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
import org.qi4j.property.Property;
import org.qi4j.property.ReadableProperty;
import org.qi4j.property.WritableProperty;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.ListAssociationInstance;
import org.qi4j.spi.entity.EntityAlreadyExistsException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;
import org.qi4j.spi.property.ReadablePropertyInstance;
import org.qi4j.spi.property.WritablePropertyInstance;
import org.qi4j.spi.serialization.SerializedEntity;
import org.qi4j.spi.serialization.SerializedState;

public class MemoryEntityStore
    implements EntityStore
{
    private Map<SerializedEntity, SerializedState> entityState;

    public MemoryEntityStore()
    {
        entityState = new ConcurrentHashMap<SerializedEntity, SerializedState>();
    }

    public boolean exists( String identity, Class<? extends EntityComposite> compositeType ) throws StoreException
    {
        return entityState.containsKey( new SerializedEntity( identity, compositeType ) );
    }

    public EntityState newEntityInstance( EntitySession session, String identity, Class compositeType, Iterable<PropertyBinding> propertyBindings, Iterable<AssociationBinding> associationBindings, Map<String, Object> propertyValues ) throws StoreException
    {
        SerializedEntity id = new SerializedEntity( identity, compositeType );
        if( entityState.containsKey( id ) )
        {
            throw new EntityAlreadyExistsException( "Memory store", identity );
        }

        Map<String, Property> properties = new HashMap<String, Property>();
        for( PropertyBinding propertyBinding : propertyBindings )
        {
            // Either use default value or the one that was set through the builder
            Object value = propertyBinding.getDefaultValue();
            if( propertyValues.containsKey( propertyBinding.getQualifiedName() ) )
            {
                value = propertyValues.get( propertyBinding.getQualifiedName() );
            }

            if( ReadableProperty.class.isAssignableFrom( propertyBinding.getPropertyResolution().getPropertyModel().getAccessor().getReturnType() ) )
            {
                properties.put( propertyBinding.getQualifiedName(), new ReadablePropertyInstance<Object>( propertyBinding, value ) );
            }
            else
            if( WritableProperty.class.isAssignableFrom( propertyBinding.getPropertyResolution().getPropertyModel().getAccessor().getReturnType() ) )
            {
                properties.put( propertyBinding.getQualifiedName(), new WritablePropertyInstance<Object>( propertyBinding, value ) );
            }
            else
            {
                properties.put( propertyBinding.getQualifiedName(), new PropertyInstance<Object>( propertyBinding, value ) );
            }
        }

        Map<String, AbstractAssociation> associations = new HashMap<String, AbstractAssociation>();
        for( AssociationBinding associationBinding : associationBindings )
        {
            if( SetAssociation.class.isAssignableFrom( associationBinding.getAssociationResolution().getAssociationModel().getAccessor().getReturnType() ) )
            {
//                associations.put( associationBinding.getQualifiedName(), new SetAssociationInstance())
            }
            else
            if( ManyAssociation.class.isAssignableFrom( associationBinding.getAssociationResolution().getAssociationModel().getAccessor().getReturnType() ) )
            {
                associations.put( associationBinding.getQualifiedName(), new ListAssociationInstance<Object>( new ArrayList<Object>(), associationBinding ) );
            }
            else
            {
                associations.put( associationBinding.getQualifiedName(), new AssociationInstance<Object>( associationBinding, null ) );
            }
        }

        MemoryEntityState entityState = new MemoryEntityState( identity, compositeType, properties, associations );

        return entityState;
    }

    public EntityState getEntityInstance( EntitySession session, String identity, Class compositeType, Iterable<PropertyBinding> propertyBindings, Iterable<AssociationBinding> associationBindings ) throws StoreException
    {
        return null;
    }

    public boolean delete( String identity, Class compositeType ) throws StoreException
    {
        return entityState.remove( new SerializedEntity( identity, compositeType ) ) != null;
    }
}
