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
import org.qi4j.api.common.TypeName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;

/**
 * If entitystore will use this class, the need to create and query the primary keys of entities will be removed from indexing, thus improving performance, possibly a lot.
 *
 * @author Stanislav Muhametsin
 */
public interface SQLEntityState extends EntityState
{

    public Long getEntityPK();

    public DefaultEntityState getDefaultEntityState();

    public final class DefaultSQLEntityState implements SQLEntityState
    {
        private final DefaultEntityState _state;

        private final long _entityPK;

        public DefaultSQLEntityState(DefaultEntityState state, long pk)
        {
            if (state == null)
            {
                throw new IllegalArgumentException( "Entity state must not be null." );
            }
            this._state = state;
            this._entityPK = pk;
        }

        @Override
        public Long getEntityPK()
        {
            return this._entityPK;
        }

        @Override
        public DefaultEntityState getDefaultEntityState()
        {
            return this._state;
        }

        @Override
        public EntityDescriptor entityDescriptor()
        {
            return this._state.entityDescriptor();
        }

        @Override
        public EntityReference getAssociation( QualifiedName stateName )
        {
            return this._state.getAssociation( stateName );
        }

        @Override
        public ManyAssociationState getManyAssociation( QualifiedName stateName )
        {
            return this._state.getManyAssociation( stateName );
        }

        @Override
        public Object getProperty( QualifiedName stateName )
        {
            return this._state.getProperty( stateName );
        }

        @Override
        public EntityReference identity()
        {
            return this._state.identity();
        }

        @Override
        public boolean isOfType( TypeName type )
        {
            return this._state.isOfType( type );
        }

        @Override
        public long lastModified()
        {
            return this._state.lastModified();
        }

        @Override
        public void remove()
        {
            this._state.remove();
        }

        @Override
        public void setAssociation( QualifiedName stateName, EntityReference newEntity )
        {
            this._state.setAssociation( stateName, newEntity );
        }

        @Override
        public void setProperty( QualifiedName stateName, Object json )
        {
            this._state.setProperty( stateName, json );
        }

        @Override
        public EntityStatus status()
        {
            return this._state.status();
        }

        @Override
        public String version()
        {
            return this._state.version();
        }

        @Override
        public boolean equals( Object obj )
        {
            return this._state.equals( obj );
        }

        @Override
        public int hashCode()
        {
            return this._state.hashCode();
        }

        @Override
        public String toString()
        {
            return this._state.toString();
        }

    }
}
