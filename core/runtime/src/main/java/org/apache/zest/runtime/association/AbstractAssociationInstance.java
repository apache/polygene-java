/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.runtime.association;

import java.lang.reflect.Type;
import org.apache.zest.api.association.AbstractAssociation;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.functional.Function2;

/**
 * Implementation of AbstractAssociation. Includes helper methods for subclasses
 */
public abstract class AbstractAssociationInstance<T>
    implements AbstractAssociation
{
    protected AssociationInfo associationInfo;
    private final Function2<EntityReference, Type, Object> entityFunction;

    public AbstractAssociationInstance( AssociationInfo associationInfo,
                                        Function2<EntityReference, Type, Object> entityFunction
    )
    {
        this.associationInfo = associationInfo;
        this.entityFunction = entityFunction;
    }

    public AssociationInfo associationInfo()
    {
        return associationInfo;
    }

    public void setAssociationInfo( AssociationInfo newInfo )
    {
        this.associationInfo = newInfo;
    }

    @SuppressWarnings( "unchecked" )
    protected T getEntity( EntityReference entityId )
    {
        if( entityId == null )
        {
            return null;
        }

        return (T) entityFunction.map( entityId, associationInfo.type() );
    }

    protected EntityReference getEntityReference( Object composite )
    {
        if( composite == null )
        {
            return null;
        }

        return new EntityReference( ( (Identity) composite ).identity().get() );
    }

    protected void checkType( Object instance )
    {

        if( instance instanceof Identity || instance == null )
        {
            return;
        }
        throw new IllegalArgumentException( "Object must be a subtype of org.qi4j.api.identity.Identity: " + instance.getClass() );
    }

    protected void checkImmutable()
        throws IllegalStateException
    {
        if( associationInfo.isImmutable() )
        {
            throw new IllegalStateException( "Association [" + associationInfo.qualifiedName() + "] is immutable." );
        }
    }
}
