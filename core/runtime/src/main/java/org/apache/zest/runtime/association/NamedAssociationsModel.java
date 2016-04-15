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
package org.apache.zest.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;
import org.apache.zest.runtime.unitofwork.ModuleUnitOfWork;
import org.apache.zest.runtime.value.ValueStateInstance;
import org.apache.zest.spi.entity.EntityState;

/**
 * Model for NamedAssociations.
 */
public final class NamedAssociationsModel
    implements VisitableHierarchy<NamedAssociationsModel, NamedAssociationModel>
{
    private final Map<AccessibleObject, NamedAssociationModel> mapAccessorAssociationModel = new LinkedHashMap<>();

    public NamedAssociationsModel()
    {
    }

    public Stream<NamedAssociationModel> namedAssociations()
    {
        return mapAccessorAssociationModel.values().stream();
    }

    public void addNamedAssociation( NamedAssociationModel model )
    {
        mapAccessorAssociationModel.put( model.accessor(), model );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super NamedAssociationsModel, ? super NamedAssociationModel, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( NamedAssociationModel associationModel : mapAccessorAssociationModel.values() )
            {
                if( !associationModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public <T> NamedAssociation<T> newInstance( AccessibleObject accessor,
                                                EntityState entityState,
                                                ModuleUnitOfWork uow )
    {
        return mapAccessorAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public NamedAssociationModel getNamedAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        if( false )
        {
            return (NamedAssociationModel) getNamedAssociationByName( QualifiedName.fromAccessor( accessor ).name() );
        }
        NamedAssociationModel namedAssociationModel = mapAccessorAssociationModel.get( accessor );
        if( namedAssociationModel == null )
        {
            throw new IllegalArgumentException( "No named-association found with name:" + ( (Member) accessor ).getName() );
        }
        return namedAssociationModel;
    }

    public AssociationDescriptor getNamedAssociationByName( String name )
        throws IllegalArgumentException
    {
        for( NamedAssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().name().equals( name ) )
            {
                return associationModel;
            }
        }
        throw new IllegalArgumentException( "No named-association found with name:" + name );
    }

    public AssociationDescriptor getNamedAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        for( NamedAssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().equals( name ) )
            {
                return associationModel;
            }
        }
        throw new IllegalArgumentException( "No named-association found with qualified name:" + name );
    }

    public void checkConstraints( ValueStateInstance state )
    {
        for( NamedAssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            associationModel.checkAssociationConstraints( state.namedAssociationFor( associationModel.accessor() ) );
        }
    }
}
