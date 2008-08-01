/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import java.util.Collection;
import org.qi4j.entity.LoadingPolicy;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public class RecordingEntityState
    implements EntityState
{
    private EntityState entityState;
    private LoadingPolicy loadingPolicy;

    public RecordingEntityState( EntityState entityState, LoadingPolicy loadingPolicy )
    {
        this.entityState = entityState;
        this.loadingPolicy = loadingPolicy;
    }

    public QualifiedIdentity getIdentity()
    {
        return entityState.getIdentity();
    }

    public long getEntityVersion()
    {
        return entityState.getEntityVersion();
    }

    public void remove()
    {
        entityState.remove();
    }

    public EntityStatus getStatus()
    {
        return entityState.getStatus();
    }

    public Object getProperty( String qualifiedName )
    {
        loadingPolicy.usesProperty( qualifiedName );
        return entityState.getProperty( qualifiedName );
    }

    public void setProperty( String qualifiedName, Object newValue )
    {
        entityState.setProperty( qualifiedName, newValue );
    }

    public QualifiedIdentity getAssociation( String qualifiedName )
    {
        return entityState.getAssociation( qualifiedName );
    }

    public void setAssociation( String qualifiedName, QualifiedIdentity newEntity )
    {
        entityState.setAssociation( qualifiedName, newEntity );
    }

    public Collection<QualifiedIdentity> getManyAssociation( String qualifiedName )
    {
        return entityState.getManyAssociation( qualifiedName );
    }

    public Collection<QualifiedIdentity> setManyAssociation( String qualifiedName, Collection<QualifiedIdentity> newManyAssociation )
    {
        return entityState.setManyAssociation( qualifiedName, newManyAssociation );
    }

    public Iterable<String> getPropertyNames()
    {
        return entityState.getPropertyNames();
    }

    public Iterable<String> getAssociationNames()
    {
        return entityState.getAssociationNames();
    }

    public Iterable<String> getManyAssociationNames()
    {
        return entityState.getManyAssociationNames();
    }
}
