/*
 * Copyright (c) 2011-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.association;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.runtime.value.ValueStateInstance;
import org.qi4j.spi.entity.EntityState;

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

    public Iterable<NamedAssociationModel> namedAssociations()
    {
        return mapAccessorAssociationModel.values();
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
        System.out.println( "######################################################################" );
        System.out.println( "GET NAMED ASSOCIATION" );
        System.out.println( "\tupon: " + mapAccessorAssociationModel );
        System.out.println( "\tfor:  " + accessor );
        System.out.println( "\treturn: "+namedAssociationModel );
        System.out.println( "######################################################################" );
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
