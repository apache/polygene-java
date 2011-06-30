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

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.association.ManyAssociationDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.AccessibleObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of Property instances.
 */
public final class ManyAssociationsInstance
{
    private Map<AccessibleObject, ManyAssociation<?>> manyAssociations;
    private ManyAssociationsModel model;
    private EntityState entityState;
    private ModuleUnitOfWork uow;

    public ManyAssociationsInstance( ManyAssociationsModel model,
                                     EntityState entityState,
                                     ModuleUnitOfWork uow
    )
    {
        this.model = model;
        this.entityState = entityState;
        this.uow = uow;
    }

    public <T> ManyAssociation<T> manyAssociationFor( AccessibleObject accessor )
    {
        if( manyAssociations == null )
        {
            manyAssociations = new HashMap<AccessibleObject, ManyAssociation<?>>();
        }

        ManyAssociation<T> association = (ManyAssociation<T>) manyAssociations.get( accessor );

        if( association == null )
        {
            association = model.newInstance( accessor, entityState, uow );
            manyAssociations.put( accessor, association );
        }

        return association;
    }

    public Iterable<ManyAssociation<?>> manyAssociations()
    {
        return Iterables.map( new Function<ManyAssociationDescriptor, ManyAssociation<?>>()
                {
                    @Override
                    public ManyAssociation<?> map( ManyAssociationDescriptor associationDescriptor )
                    {
                        return manyAssociationFor( associationDescriptor.accessor() );
                    }
                }, model.<ManyAssociationDescriptor>manyAssociations() );
    }

    public void checkConstraints()
    {
        model.checkConstraints( this );
    }
}