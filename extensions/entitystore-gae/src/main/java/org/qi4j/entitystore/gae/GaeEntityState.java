/*
 * Copyright 2010 Niclas Hedhman.
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

package org.qi4j.entitystore.gae;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

import static org.qi4j.functional.Iterables.first;

public class GaeEntityState
    implements EntityState
{
    static final String PROPERTY_TYPE = "$type";

    private final Entity entity;
    private EntityStatus status;
    private final GaeEntityStoreUnitOfWork unitOfWork;
    private final ValueSerialization valueSerialization;
    private final EntityDescriptor descriptor;
    private final HashMap<QualifiedName, ValueType> valueTypes;
    private final Module module;

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork,
                           ValueSerialization valueSerialization,
                           Key key,
                           EntityDescriptor descriptor,
                           Module module )
    {
        System.out.println( "GaeEntityState( " + unitOfWork + ", " + key + ", " + descriptor + " )" );
        this.module = module;
        this.unitOfWork = unitOfWork;
        this.valueSerialization = valueSerialization;
        this.descriptor = descriptor;
        entity = new Entity( key );
        entity.setProperty( "$version", unitOfWork.identity() );
        Class type = first( descriptor.types() );
        String name = type.getName();
        System.out.println( "New Entity\n" +
                            "    descriptor:" + descriptor + "\n  " +
                            "    entityType:" + name + "\n  " +
                            "    name:" + name + "\n  "
        );
        entity.setUnindexedProperty( PROPERTY_TYPE, name );
        status = EntityStatus.NEW;
        valueTypes = initializeValueTypes( descriptor );
    }

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork,
                           ValueSerialization valueSerialization,
                           Entity entity,
                           Module module )
    {
        System.out.println( "GaeEntityState( " + unitOfWork + ", " + entity + " )" );
        if( entity == null )
        {
            throw new NullPointerException();
        }
        if( unitOfWork == null )
        {
            throw new NullPointerException();
        }
        this.module = module;
        this.unitOfWork = unitOfWork;
        this.valueSerialization = valueSerialization;
        this.entity = entity;
        String typeName = (String) entity.getProperty( GaeEntityState.PROPERTY_TYPE );
        System.out.println( "LOADING [" + typeName + "]" );
        descriptor = module.entityDescriptor( typeName );
        status = EntityStatus.LOADED;
        valueTypes = initializeValueTypes( descriptor );
    }

    private HashMap<QualifiedName, ValueType> initializeValueTypes( EntityDescriptor descriptor )
    {
        HashMap<QualifiedName, ValueType> result = new HashMap<QualifiedName, ValueType>();
        for( PropertyDescriptor persistent : descriptor.state().properties() )
        {
            if( persistent.valueType() instanceof ValueCompositeType )
            {
                QualifiedName name = persistent.qualifiedName();
                result.put( name, persistent.valueType() );
            }
        }
        return result;
    }

    Entity entity()
    {
        System.out.println( "entity()  -->  " + entity );
        return entity;
    }

    @Override
    public EntityReference identity()
    {
        EntityReference ref = new EntityReference( entity.getKey().getName() );
        System.out.println( "identity()  -->  " + ref );
        return ref;
    }

    @Override
    public String version()
    {
        String version = (String) entity.getProperty( "$version" );
        System.out.println( "version()  -->  " + version );
        return version;
    }

    @Override
    public long lastModified()
    {
        Long lastModified = (Long) entity.getProperty( "$lastModified" );
        System.out.println( "lastModified()  -->  " + lastModified );
        return lastModified;
    }

    @Override
    public void remove()
    {
        System.out.println( "remove()" );
        status = EntityStatus.REMOVED;
    }

    @Override
    public EntityStatus status()
    {
        System.out.println( "status()  -->  " + status );
        return status;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        System.out.println( "isAssignableTo( " + type + " )  -->  false" );
        return false;
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        System.out.println( "entityDescriptor()  -->  " + descriptor );
        return descriptor;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        String uri = stateName.toURI();
        Object value = entity.getProperty( uri );
        if( value instanceof Text )
        {
            value = ( (Text) value ).getValue();
        }
        ValueType type = valueTypes.get( stateName );
        if( value != null && type != null )
        {
            try
            {
                value = valueSerialization.deserialize( type, value.toString() );
            }
            catch( ValueSerializationException e )
            {
                String message = "\nqualifiedName: " + stateName +
                                 "\n    stateName: " + stateName.name() +
                                 "\n          uri: " + uri +
                                 "\n         type: " + type +
                                 "\n        value: " + value +
                                 "\n";
                InternalError error = new InternalError( message );
                error.initCause( e );
                throw error;
            }
        }
        System.out.println( "getProperty( " + stateName + " )  -->  " + uri + "=" + value );
        return value;
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        System.out.println( "setProperty( " + stateName + ", " + newValue + " )" );
        Object value = null;
        if( newValue == null || ValueType.isPrimitiveValue( newValue ) )
        {
            value = newValue;
        }
        else
        {
            try
            {
                value = valueSerialization.serialize( newValue );
            }
            catch( ValueSerializationException e )
            {
                String message = "\nqualifiedName: " + stateName +
                                 "\n    stateName: " + stateName.name() +
                                 "\n        class: " + newValue.getClass() +
                                 "\n        value: " + value +
                                 "\n";
                InternalError error = new InternalError( message );
                error.initCause( e );
                throw error;
            }
        }
        if( value instanceof String )
        {
            value = new Text( (String) value );
        }
        entity.setUnindexedProperty( stateName.toURI(), value );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        String uri = stateName.toURI();
        String identity = (String) entity.getProperty( uri );
        System.out.println( "association( " + stateName + " )  -->  " + uri + " = " + identity );
        EntityReference ref = new EntityReference( identity );
        return ref;
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        System.out.println( "setAssociation( " + stateName + ", " + newEntity + " )" );
        String uri = stateName.toURI();
        String id = null;
        if( newEntity != null )
        {
            id = newEntity.identity();
        }
        entity.setUnindexedProperty( uri, id );
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        List<String> assocs = (List<String>) entity.getProperty( stateName.toURI() );
        ManyAssociationState state = new GaeManyAssociationState( this, assocs );
        return state;
    }

    public void hasBeenApplied()
    {
        System.out.println( "hasBeenApplied()" );
        status = EntityStatus.LOADED;
    }

    private static class GaeManyAssociationState
        implements ManyAssociationState
    {
        private List<String> assocs;
        private final GaeEntityState entityState;

        public GaeManyAssociationState( GaeEntityState entityState, List<String> listOfAssociations )
        {
            this.entityState = entityState;
            if( listOfAssociations == null )
            {
                this.assocs = new ArrayList<String>();
            }
            else
            {
                this.assocs = listOfAssociations;
            }
        }

        @Override
        public int count()
        {
            return assocs.size();
        }

        @Override
        public boolean contains( EntityReference entityReference )
        {
            return assocs.contains( entityReference.identity() );
        }

        @Override
        public boolean add( int index, EntityReference entityReference )
        {
            System.out.println( "NICLAS::" + entityReference );
            String identity = entityReference.identity();
            System.out.println( "NICLAS::" + identity );
            System.out.println( "NICLAS::" + assocs );
            if( assocs.contains( identity ) )
            {
                return false;
            }
            assocs.add( index, entityReference.identity() );
            entityState.markUpdated();
            return true;
        }

        @Override
        public boolean remove( EntityReference entityReference )
        {
            return assocs.remove( entityReference.identity() );
        }

        @Override
        public EntityReference get( int index )
        {
            String id = assocs.get( index );
            return new EntityReference( id );
        }

        @Override
        public Iterator<EntityReference> iterator()
        {
            ArrayList<EntityReference> result = new ArrayList<EntityReference>();
            for( String id : assocs )
            {
                result.add( new EntityReference( id ) );
            }
            return result.iterator();
        }
    }

    private void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }
}
