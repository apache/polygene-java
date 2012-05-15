/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.sql.api;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;

/**
 * If entitystore will use this class, the need to create and query the primary keys of entities will be removed from indexing, thus improving performance, possibly a lot.
 * 
 * TODO QueryEntityPK
 */
public interface SQLEntityState
        extends EntityState
{

    public Long getEntityPK();

    public Long getEntityOptimisticLock();

    public DefaultEntityState getDefaultEntityState();

    @SuppressWarnings( "PublicInnerClass" )
    public final class DefaultSQLEntityState
            implements SQLEntityState
    {

        private final DefaultEntityState state;

        private final Long entityPK;

        private final Long entityOptimisticLock;

        public DefaultSQLEntityState( DefaultEntityState state, Long entityPK, Long entityOptimisticLock )
        {
            NullArgumentException.validateNotNull( "Entity state", state );
            NullArgumentException.validateNotNull( "Entity PK", entityPK );
            this.state = state;
            this.entityPK = entityPK;
            this.entityOptimisticLock = entityOptimisticLock;
        }

        public Long getEntityPK()
        {
            return entityPK;
        }

        public Long getEntityOptimisticLock()
        {
            return entityOptimisticLock;
        }

        public DefaultEntityState getDefaultEntityState()
        {
            return state;
        }

        public EntityDescriptor entityDescriptor()
        {
            return state.entityDescriptor();
        }

        public EntityReference getAssociation( QualifiedName stateName )
        {
            return state.getAssociation( stateName );
        }

        public ManyAssociationState getManyAssociation( QualifiedName stateName )
        {
            return state.getManyAssociation( stateName );
        }

        public Object getProperty( QualifiedName stateName )
        {
            return state.getProperty( stateName );
        }

        public EntityReference identity()
        {
            return state.identity();
        }

        @Override
        public boolean isAssignableTo( Class<?> type )
        {
            return state.isAssignableTo( type );
        }

        public long lastModified()
        {
            return state.lastModified();
        }

        public void remove()
        {
            state.remove();
        }

        public void setAssociation( QualifiedName stateName, EntityReference newEntity )
        {
            state.setAssociation( stateName, newEntity );
        }

        public void setProperty( QualifiedName stateName, Object json )
        {
            state.setProperty( stateName, json );
        }

        public EntityStatus status()
        {
            return state.status();
        }

        public String version()
        {
            return state.version();
        }

        @Override
        @SuppressWarnings( "EqualsWhichDoesntCheckParameterClass" )
        public boolean equals( Object obj )
        {
            return state.equals( obj );
        }

        @Override
        public int hashCode()
        {
            return state.hashCode();
        }

        @Override
        public String toString()
        {
            return state.toString();
        }

    }

}
