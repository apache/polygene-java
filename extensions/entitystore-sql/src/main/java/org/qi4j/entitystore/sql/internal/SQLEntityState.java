/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.entitystore.sql.internal;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;

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

        public DefaultSQLEntityState( DefaultEntityState state )
        {
            NullArgumentException.validateNotNull( "Entity state", state );
            this.state = state;
            this.entityPK = null;
            this.entityOptimisticLock = null;
        }

        public DefaultSQLEntityState( DefaultEntityState state, Long entityPK, Long entityOptimisticLock )
        {
            NullArgumentException.validateNotNull( "Entity state", state );
            NullArgumentException.validateNotNull( "Entity PK", entityPK );
            NullArgumentException.validateNotNull( "Entity OptimisticLock", entityOptimisticLock );
            this.state = state;
            this.entityPK = entityPK;
            this.entityOptimisticLock = entityOptimisticLock;
        }

        @Override
        public Long getEntityPK()
        {
            return entityPK;
        }

        @Override
        public Long getEntityOptimisticLock()
        {
            return entityOptimisticLock;
        }

        @Override
        public DefaultEntityState getDefaultEntityState()
        {
            return state;
        }

        @Override
        public EntityDescriptor entityDescriptor()
        {
            return state.entityDescriptor();
        }

        @Override
        public EntityReference associationValueOf( QualifiedName stateName )
        {
            return state.associationValueOf( stateName );
        }

        @Override
        public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
        {
            return state.manyAssociationValueOf( stateName );
        }

        @Override
        public Object propertyValueOf( QualifiedName stateName )
        {
            return state.propertyValueOf( stateName );
        }

        @Override
        public EntityReference identity()
        {
            return state.identity();
        }

        @Override
        public boolean isAssignableTo( Class<?> type )
        {
            return state.isAssignableTo( type );
        }

        @Override
        public long lastModified()
        {
            return state.lastModified();
        }

        @Override
        public void remove()
        {
            state.remove();
        }

        @Override
        public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
        {
            state.setAssociationValue( stateName, newEntity );
        }

        @Override
        public void setPropertyValue( QualifiedName stateName, Object json )
        {
            state.setPropertyValue( stateName, json );
        }

        @Override
        public EntityStatus status()
        {
            return state.status();
        }

        @Override
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
