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
package org.apache.polygene.runtime.entity;

import java.lang.reflect.AccessibleObject;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.AssociationStateDescriptor;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.association.AssociationModel;
import org.apache.polygene.runtime.association.AssociationsModel;
import org.apache.polygene.runtime.association.ManyAssociationModel;
import org.apache.polygene.runtime.association.ManyAssociationsModel;
import org.apache.polygene.runtime.association.NamedAssociationModel;
import org.apache.polygene.runtime.association.NamedAssociationsModel;
import org.apache.polygene.runtime.composite.StateModel;
import org.apache.polygene.runtime.property.PropertiesModel;

/**
 * Model for EntityComposite state.
 */
public final class EntityStateModel extends StateModel
    implements AssociationStateDescriptor
{
    private final AssociationsModel associationsModel;
    private final ManyAssociationsModel manyAssociationsModel;
    private final NamedAssociationsModel namedAssociationsModel;

    public EntityStateModel( PropertiesModel propertiesModel,
                             AssociationsModel associationsModel,
                             ManyAssociationsModel manyAssociationsModel,
                             NamedAssociationsModel namedAssociationsModel )
    {
        super( propertiesModel );
        this.associationsModel = associationsModel;
        this.manyAssociationsModel = manyAssociationsModel;
        this.namedAssociationsModel = namedAssociationsModel;
    }

    public AssociationModel getAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        return associationsModel.getAssociation( accessor );
    }

    @Override
    public AssociationDescriptor getAssociationByName( String name )
        throws IllegalArgumentException
    {
        return associationsModel.getAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        return associationsModel.getAssociationByQualifiedName( name );
    }

    @Override
    public boolean hasAssociation( QualifiedName name )
    {
        return associationsModel.hasAssociation( name );
    }

    public ManyAssociationModel getManyAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        return manyAssociationsModel.getManyAssociation( accessor );
    }

    @Override
    public AssociationDescriptor getManyAssociationByName( String name )
        throws IllegalArgumentException
    {
        return manyAssociationsModel.getManyAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        return manyAssociationsModel.getManyAssociationByQualifiedName( name );
    }

    @Override
    public boolean hasManyAssociation( QualifiedName name )
    {
        return manyAssociationsModel.hasAssociation( name );
    }

    public NamedAssociationModel getNamedAssociation( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        return namedAssociationsModel.getNamedAssociation( accessor );
    }

    @Override
    public AssociationDescriptor getNamedAssociationByName( String name )
        throws IllegalArgumentException
    {
        return namedAssociationsModel.getNamedAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getNamedAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        return namedAssociationsModel.getNamedAssociationByQualifiedName( name );
    }

    @Override
    public boolean hasNamedAssociation( QualifiedName name )
    {
        return namedAssociationsModel.hasAssociation( name );
    }

    @Override
    public Stream<AssociationModel> associations()
    {
        return associationsModel.associations();
    }

    @Override
    public Stream<ManyAssociationModel> manyAssociations()
    {
        return manyAssociationsModel.manyAssociations();
    }

    @Override
    public Stream<NamedAssociationModel> namedAssociations()
    {
        return namedAssociationsModel.namedAssociations();
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
                    if( ( (VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>) manyAssociationsModel ).accept( visitor ) )
                    {
                        ( (VisitableHierarchy<NamedAssociationsModel, NamedAssociationModel>) namedAssociationsModel ).accept( visitor );
                    }
                }
            }
        }
        return visitor.visitLeave( this );
    }

}
