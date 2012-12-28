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

package org.qi4j.runtime.value;

import java.util.List;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.property.PropertiesModel;

/**
 * JAVADOC
 */
public final class ValueStateModel
    extends StateModel
    implements AssociationStateDescriptor
{
    private final AssociationsModel associationsModel;
    private final ManyAssociationsModel manyAssociationsModel;

    public ValueStateModel( PropertiesModel propertiesModel,
                            AssociationsModel associationsModel,
                            ManyAssociationsModel manyAssociationsModel
    )
    {
        super( propertiesModel );
        this.associationsModel = associationsModel;
        this.manyAssociationsModel = manyAssociationsModel;
    }

    @Override
    public AssociationDescriptor getAssociationByName( String name )
    {
        return associationsModel.getAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
    {
        return associationsModel.getAssociationByQualifiedName( name );
    }

    @Override
    public AssociationDescriptor getManyAssociationByName( String name )
    {
        return manyAssociationsModel.getManyAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
    {
        return manyAssociationsModel.getManyAssociationByQualifiedName( name );
    }

    @Override
    public Iterable<AssociationModel> associations()
    {
        return associationsModel.associations();
    }

    @Override
    public Iterable<ManyAssociationModel> manyAssociations()
    {
        return manyAssociationsModel.manyAssociations();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( ( (VisitableHierarchy<Object, Object>) propertiesModel ).accept( visitor ) )
            {
                if( ( (VisitableHierarchy<AssociationsModel, AssociationModel>) associationsModel ).accept( visitor ) )
                {
                    ( (VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>) manyAssociationsModel ).accept( visitor );
                }
            }
        }

        return visitor.visitLeave( this );
    }

    public interface StateResolver
    {
        public Object getPropertyState( PropertyDescriptor propertyDescriptor );

        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor );

        public List<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor );
    }
}
