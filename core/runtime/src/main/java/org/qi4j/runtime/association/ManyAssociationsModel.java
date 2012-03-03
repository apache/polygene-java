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

package org.qi4j.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.value.ValueStateInstance;
import org.qi4j.spi.entity.EntityState;

/**
 * JAVADOC
 */
public final class ManyAssociationsModel
    implements VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>
{
    private final Map<AccessibleObject, ManyAssociationModel> mapAccessorAssociationModel = new LinkedHashMap<AccessibleObject, ManyAssociationModel>();

    public ManyAssociationsModel(
    )
    {
    }

    public void addManyAssociation( ManyAssociationModel model )
    {
        mapAccessorAssociationModel.put( model.accessor(), model );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super ManyAssociationsModel, ? super ManyAssociationModel, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ManyAssociationModel associationModel : mapAccessorAssociationModel.values() )
            {
                if( !associationModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public ManyAssociationModel getManyAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        ManyAssociationModel manyAssociationModel = mapAccessorAssociationModel.get( accessor );
        if( manyAssociationModel == null )
        {
            throw new IllegalArgumentException( "No many-association found with name:" + ( (Member) accessor ).getName() );
        }
        return manyAssociationModel;
    }

    public Iterable<ManyAssociationModel> manyAssociations()
    {
        return mapAccessorAssociationModel.values();
    }

    public <T> ManyAssociation<T> newInstance( AccessibleObject accessor,
                                               EntityState entityState,
                                               ModuleUnitOfWork uow
    )
    {
        return mapAccessorAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public AssociationDescriptor getManyAssociationByName( String name )
        throws IllegalArgumentException
    {
        for( ManyAssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().name().equals( name ) )
            {
                return associationModel;
            }
        }

        throw new IllegalArgumentException( "No many-association found with name:" + name );
    }

    public AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        for( ManyAssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().equals( name ) )
            {
                return associationModel;
            }
        }

        throw new IllegalArgumentException( "No many-association found with qualified name:" + name );
    }

    public void checkConstraints( ValueStateInstance state )
    {
        for( ManyAssociationModel manyAssociationModel : mapAccessorAssociationModel.values() )
        {
            manyAssociationModel.checkAssociationConstraints( state.manyAssociationFor( manyAssociationModel.accessor() ) );
        }
    }
}