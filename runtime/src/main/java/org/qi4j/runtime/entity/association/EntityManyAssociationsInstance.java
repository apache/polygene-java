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

package org.qi4j.runtime.entity.association;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;

/**
 * Collection of Property instances.
 */
public final class EntityManyAssociationsInstance
{
    private Map<Method, ManyAssociation<?>> manyAssociations;
    private EntityManyAssociationsModel model;
    private EntityState entityState;
    private ModuleUnitOfWork uow;

    public EntityManyAssociationsInstance( EntityManyAssociationsModel model,
                                           EntityState entityState,
                                           ModuleUnitOfWork uow
    )
    {
        this.model = model;
        this.entityState = entityState;
        this.uow = uow;
    }

    public <T> ManyAssociation<T> manyAssociationFor( Method accessor )
    {
        if( manyAssociations == null )
        {
            manyAssociations = new HashMap<Method, ManyAssociation<?>>();
        }

        ManyAssociation<T> association = (ManyAssociation<T>) manyAssociations.get( accessor );

        if( association == null )
        {
            association = model.newInstance( accessor, entityState, uow );
            manyAssociations.put( accessor, association );
        }

        return association;
    }

    public void checkConstraints()
    {
        model.checkConstraints( this );
    }

    public void visitManyAssociations( EntityStateHolder.EntityStateVisitor visitor )
    {
        for( ManyAssociationDescriptor manyAssociationDescriptor : model.manyAssociations() )
        {
            visitor.visitManyAssociation( manyAssociationDescriptor.qualifiedName(), manyAssociationFor( manyAssociationDescriptor.accessor() ) );
        }
    }
}