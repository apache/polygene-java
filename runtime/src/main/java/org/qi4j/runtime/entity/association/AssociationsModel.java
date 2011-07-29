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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.AssociationDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.AccessibleObject;
import java.util.*;

/**
 * JAVADOC
 */
public final class AssociationsModel
    implements VisitableHierarchy<AssociationsModel, AssociationModel>
{
    private final Set<AccessibleObject> accessors = new HashSet<AccessibleObject>();
    private final Set<AssociationModel> associationModels = new LinkedHashSet<AssociationModel>();
    private final Map<AccessibleObject, AssociationModel> mapAccessorAssociationModel = new HashMap<AccessibleObject, AssociationModel>();
    private final Map<QualifiedName, AccessibleObject> mapQualifiedNameAccessors = new HashMap<QualifiedName, AccessibleObject>();

    public AssociationsModel(  )
    {
    }

    public void addAssociation(AssociationModel associationModel)
    {
        associationModels.add( associationModel );
        mapAccessorAssociationModel.put( associationModel.accessor(), associationModel );
        mapQualifiedNameAccessors.put( associationModel.qualifiedName(), associationModel.accessor() );
    }

    public <T extends AssociationDescriptor> Set<T> associations()
    {
        return (Set<T>) associationModels;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super AssociationsModel, ? super AssociationModel, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            for( AssociationModel associationModel : associationModels )
            {
                if (!associationModel.accept(visitor))
                    break;
            }
        }
        return visitor.visitLeave( this );
    }

    public <T> Association<T> newInstance( AccessibleObject accessor, EntityState entityState, ModuleUnitOfWork uow )
    {
        return mapAccessorAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public AssociationDescriptor getAssociationByName( String name )
    {
        for( AssociationModel associationModel : associationModels )
        {
            if( associationModel.qualifiedName().name().equals( name ) )
            {
                return associationModel;
            }
        }

        return null;
    }

    public void checkConstraints( AssociationsInstance associations )
    {
        for( AssociationModel associationModel : associationModels )
        {
            associationModel.checkAssociationConstraints( associations );
            associationModel.checkConstraints( associations );
        }
    }
}
