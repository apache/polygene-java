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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.AssociationDescriptor;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.AccessibleObject;
import java.util.*;

/**
 * JAVADOC
 */
public final class ManyAssociationsModel
    implements VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>
{
    private final Set<AccessibleObject> accessors = new HashSet<AccessibleObject>();
    private final Set<ManyAssociationModel> manyAssociationModels = new LinkedHashSet<ManyAssociationModel>();
    private final Map<AccessibleObject, ManyAssociationModel> mapAccessorAssociationModel = new HashMap<AccessibleObject, ManyAssociationModel>();
    private final Map<QualifiedName, AccessibleObject> mapQualifiedNameAccessor = new HashMap<QualifiedName, AccessibleObject>();

    public ManyAssociationsModel(
    )
    {
    }

    public void addManyAssociation( ManyAssociationModel model )
    {
        manyAssociationModels.add( model );
        mapAccessorAssociationModel.put( model.accessor(), model );
        mapQualifiedNameAccessor.put( model.qualifiedName(), model.accessor() );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super ManyAssociationsModel, ? super ManyAssociationModel, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            for( ManyAssociationModel associationModel : manyAssociationModels )
            {
                if (!associationModel.accept(visitor))
                    break;
            }
        }
        return visitor.visitLeave( this );
    }

    public <T extends AssociationDescriptor> Set<T> manyAssociations()
    {
        return (Set<T>) manyAssociationModels;
    }

    public <T> ManyAssociation<T> newInstance( AccessibleObject accessor, EntityState entityState, ModuleUnitOfWork uow )
    {
        return mapAccessorAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public AssociationDescriptor getManyAssociationByName( String name )
    {
        for( ManyAssociationModel associationModel : manyAssociationModels )
        {
            if( associationModel.qualifiedName().name().equals( name ) )
            {
                return associationModel;
            }
        }

        return null;
    }

    public void checkConstraints( ManyAssociationsInstance manyAssociationsInstance )
    {
        for( ManyAssociationModel manyAssociationModel : manyAssociationModels )
        {
            ManyAssociation manyAssociation = manyAssociationsInstance.manyAssociationFor( manyAssociationModel.accessor() );
            manyAssociationModel.checkAssociationConstraints( manyAssociation );
        }
    }
}