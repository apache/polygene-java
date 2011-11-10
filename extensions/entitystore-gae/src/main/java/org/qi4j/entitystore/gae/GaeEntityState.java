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
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.json.JSONWriterSerializer;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GaeEntityState
    implements EntityState
{
    static final String PROPERTY_TYPE = "$type";

    private final Entity entity;
    private EntityStatus status;
    private final GaeEntityStoreUnitOfWork unitOfWork;
    private final EntityDescriptor descriptor;
    private final HashMap<QualifiedName, ValueType> valueTypes;
    private final Module module;
    private JSONDeserializer deserializer;

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork, Key key, EntityDescriptor descriptor, Module module )
    {
        System.out.println( "GaeEntityState( " + unitOfWork + ", " + key + ", " + descriptor + " )" );
        this.module = module;
        this.unitOfWork = unitOfWork;
        this.descriptor = descriptor;
        entity = new Entity( key );
        entity.setProperty( "$version", unitOfWork.identity() );
        Class type = descriptor.type();
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

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork, Entity entity, Module module )
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
        this.entity = entity;
        String typeName = (String) entity.getProperty( GaeEntityState.PROPERTY_TYPE );
        System.out.println( "LOADING [" + typeName + "]" );
        descriptor = module.entityDescriptor( typeName );
        status = EntityStatus.LOADED;
        valueTypes = initializeValueTypes( descriptor );
        deserializer = new JSONDeserializer( module );
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

    public EntityReference identity()
    {
        EntityReference ref = new EntityReference( entity.getKey().getName() );
        System.out.println( "identity()  -->  " + ref );
        return ref;
    }

    public String version()
    {
        String version = (String) entity.getProperty( "$version" );
        System.out.println( "version()  -->  " + version );
        return version;
    }

    public long lastModified()
    {
        Long lastModified = (Long) entity.getProperty( "$lastModified" );
        System.out.println( "lastModified()  -->  " + lastModified );
        return lastModified;
    }

    public void remove()
    {
        System.out.println( "remove()" );
        status = EntityStatus.REMOVED;
    }

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

    public EntityDescriptor entityDescriptor()
    {
        System.out.println( "entityDescriptor()  -->  " + descriptor );
        return descriptor;
    }

    public Object getProperty( QualifiedName stateName )
    {
        String uri = stateName.toURI();
        Object value = entity.getProperty( uri );
        if( value instanceof Text )
        {
            value = ( (Text) value ).getValue();
        }
        ValueType type = valueTypes.get( stateName );

        if( type != null )
        {
            try
            {
                JSONObject json = new JSONObject( value );
                value = deserializer.deserialize( json, type );
            }
            catch( JSONException e )
            {
                String message = "\nqualifiedName: " + stateName +
                                 "\n    stateName: " + stateName.name() +
                                 "\n          uri: " + uri +
                                 "\n         type: " + type +
                                 "\n        value: " + value +
                                 "\n"
                    ;
                InternalError error = new InternalError( message );
                error.initCause( e );
                throw error;
            }
        }
        System.out.println( "getProperty( " + stateName + " )  -->  " + uri + "=" + value );
        return value;
    }

    public void setProperty( QualifiedName stateName, Object value )
    {
        System.out.println( "setProperty( " + stateName + ", " + value + " )" );
        if( value != null && Proxy.isProxyClass( value.getClass() ) )
        {
            System.out.println( "handler: " + Proxy.getInvocationHandler( value ) );
            ValueType type = valueTypes.get( stateName );
            try
            {
                JSONWriterSerializer serializer = new JSONWriterSerializer(  );
                serializer.serialize( value, type );
                value = serializer.getJSON().toString();
            }
            catch( JSONException e )
            {
                InternalError error = new InternalError();
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

    public EntityReference getAssociation( QualifiedName stateName )
    {
        String uri = stateName.toURI();
        String identity = (String) entity.getProperty( uri );
        System.out.println( "getAssociation( " + stateName + " )  -->  " + uri + " = " + identity );
        EntityReference ref = new EntityReference( identity );
        return ref;
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
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

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
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

        public int count()
        {
            return assocs.size();
        }

        public boolean contains( EntityReference entityReference )
        {
            return assocs.contains( entityReference.identity() );
        }

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

        public boolean remove( EntityReference entityReference )
        {
            return assocs.remove( entityReference.identity() );
        }

        public EntityReference get( int index )
        {
            String id = assocs.get( index );
            return new EntityReference( id );
        }

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
