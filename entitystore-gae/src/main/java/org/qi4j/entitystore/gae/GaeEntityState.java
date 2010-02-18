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
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;

public class GaeEntityState
    implements EntityState
{
    private Entity entity;
    private EntityStatus status;
    private String version;

    private GaeEntityStoreUnitOfWork unitOfWork;

    private EntityDescriptor descriptor;

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork, Key key, EntityDescriptor descriptor )
    {
        this.unitOfWork = unitOfWork;
        this.descriptor = descriptor;
        entity = new Entity(key);
        status = EntityStatus.NEW;
    }

    public GaeEntityState( GaeEntityStoreUnitOfWork unitOfWork, Entity entity )
    {
        this.unitOfWork = unitOfWork;
        this.entity = entity;
        status = EntityStatus.LOADED;
    }

    Entity entity()
    {
        return entity;
    }

    public EntityReference identity()
    {
        return new EntityReference( entity.getKey().getName() );
    }

    public String version()
    {
        return (String) entity.getProperty( "$version" );
    }

    public long lastModified()
    {
        return (Long) entity.getProperty( "$lastModified" );
    }

    public void remove()
    {
         status = EntityStatus.REMOVED;
    }

    public EntityStatus status()
    {
        return status;
    }

    public boolean isOfType( TypeName type )
    {
        return false;
    }

    public EntityDescriptor entityDescriptor()
    {
        return (EntityDescriptor) entity.getProperty( "$entityDescriptor" );
    }

    public Object getProperty( QualifiedName stateName )
    {
        return entity.getProperty( stateName.toURI() );
    }

    public void setProperty( QualifiedName stateName, Object json )
    {
        entity.setUnindexedProperty( stateName.toURI(), json );
    }

    public EntityReference getAssociation( QualifiedName stateName )
    {
        String identity = (String) entity.getProperty( stateName.toURI() );
        EntityReference ref = new EntityReference( identity );
        return ref;
    }

    public void setAssociation( QualifiedName stateName, EntityReference newEntity )
    {
        entity.setUnindexedProperty( stateName.toURI(), newEntity.identity() );
    }

    public ManyAssociationState getManyAssociation( QualifiedName stateName )
    {
        List<String> assocs = (List<String>) entity.getProperty( stateName.toURI() );
        ManyAssociationState state = new GaeManyAssociationState( this, assocs );
        return state;
    }

    public void hasBeenApplied()
    {
        status = EntityStatus.LOADED;
        version = unitOfWork.identity();
    }

    private static class GaeManyAssociationState
        implements ManyAssociationState
    {
        private final List<String> assocs;
        private GaeEntityState entityState;

        public GaeManyAssociationState( GaeEntityState entityState, List<String> assocs )
        {
            this.entityState = entityState;
            this.assocs = assocs;
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
            if( assocs.contains( entityReference ) )
                    {
                        return false;
                    }
            assocs.add( index, entityReference.identity() );
            entityState.markUpdated();
            return true;
        }

        public boolean remove( EntityReference entityReference )
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public EntityReference get( int index )
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterator<EntityReference> iterator()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
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
