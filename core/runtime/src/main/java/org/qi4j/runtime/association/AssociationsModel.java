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
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class AssociationsModel
    implements VisitableHierarchy<AssociationsModel, AssociationModel>
{
    private final Map<AccessibleObject, AssociationModel> mapAccessorAssociationModel = new LinkedHashMap<AccessibleObject, AssociationModel>();

    public AssociationsModel()
    {
    }

    public void addAssociation( AssociationModel associationModel )
    {
        mapAccessorAssociationModel.put( associationModel.accessor(), associationModel );
    }

    public Iterable<AssociationModel> associations()
    {
        return mapAccessorAssociationModel.values();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super AssociationsModel, ? super AssociationModel, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( AssociationModel associationModel : mapAccessorAssociationModel.values() )
            {
                if( !associationModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public AssociationModel getAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        AssociationModel associationModel = mapAccessorAssociationModel.get( accessor );
        if( associationModel == null )
        {
            throw new IllegalArgumentException( "No association found with name:" + ( (Member) accessor ).getName() );
        }
        return associationModel;
    }

    public AssociationDescriptor getAssociationByName( String name )
        throws IllegalArgumentException
    {
        for( AssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().name().equals( name ) )
            {
                return associationModel;
            }
        }

        throw new IllegalArgumentException( "No association found with name:" + name );
    }

    public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        for( AssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            if( associationModel.qualifiedName().equals( name ) )
            {
                return associationModel;
            }
        }

        throw new IllegalArgumentException( "No association found with qualified name:" + name );
    }

    public void checkConstraints( AssociationStateHolder state )
    {
        for( AssociationModel associationModel : mapAccessorAssociationModel.values() )
        {
            Association<Object> association = state.<Object>associationFor( associationModel.accessor() );
            associationModel.checkAssociationConstraints( association );
            associationModel.checkConstraints( association.get() );
        }
    }
}
