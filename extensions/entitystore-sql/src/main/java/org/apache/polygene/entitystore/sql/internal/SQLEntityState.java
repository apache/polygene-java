/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.entitystore.sql.internal;

import java.time.Instant;
import java.util.Objects;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.spi.entity.EntityState;
import org.apache.polygene.spi.entity.EntityStatus;
import org.apache.polygene.spi.entity.ManyAssociationState;
import org.apache.polygene.spi.entity.NamedAssociationState;
import org.apache.polygene.spi.entitystore.helpers.DefaultEntityState;

public interface SQLEntityState
        extends EntityState
{

    Long getEntityPK();

    Long getEntityOptimisticLock();

    DefaultEntityState getDefaultEntityState();

    @SuppressWarnings( "PublicInnerClass" )
    public final class DefaultSQLEntityState
            implements SQLEntityState
    {

        private final DefaultEntityState state;

        private final Long entityPK;

        private final Long entityOptimisticLock;

        public DefaultSQLEntityState( DefaultEntityState state )
        {
            Objects.requireNonNull( state, "Entity state" );
            this.state = state;
            this.entityPK = null;
            this.entityOptimisticLock = null;
        }

        public DefaultSQLEntityState( DefaultEntityState state, Long entityPK, Long entityOptimisticLock )
        {
            Objects.requireNonNull( state, "Entity state" );
            Objects.requireNonNull( entityPK, "Entity PK" );
            Objects.requireNonNull( entityOptimisticLock, "Entity OptimisticLock" );
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
        public NamedAssociationState namedAssociationValueOf( QualifiedName stateName )
        {
            return state.namedAssociationValueOf( stateName );
        }

        @Override
        public Object propertyValueOf( QualifiedName stateName )
        {
            return state.propertyValueOf( stateName );
        }

        @Override
        public EntityReference entityReference()
        {
            return state.entityReference();
        }

        @Override
        public boolean isAssignableTo( Class<?> type )
        {
            return state.isAssignableTo( type );
        }

        @Override
        public Instant lastModified()
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
