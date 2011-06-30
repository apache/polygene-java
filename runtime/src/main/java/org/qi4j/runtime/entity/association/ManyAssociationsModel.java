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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.association.ManyAssociationDescriptor;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.*;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;

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
    private final ConstraintsModel constraints;
    private final ManyAssociationDeclarations manyAssociationDeclarations;

    public ManyAssociationsModel( ConstraintsModel constraints,
                                  ManyAssociationDeclarations manyAssociationDeclarations
    )
    {
        this.constraints = constraints;
        this.manyAssociationDeclarations = manyAssociationDeclarations;
    }

    public void addManyAssociationFor( AccessibleObject accessor )
    {
        if( !accessors.contains( accessor ) )
        {
            if( ManyAssociation.class.isAssignableFrom( Classes.RAW_CLASS.map( Classes.TYPE_OF.map( accessor ) ) ))
            {
                Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations(  accessor );
                boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

                // Constraints for entities in ManyAssociation
                ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericAssociationInfo
                    .getAssociationType( accessor ), ((Member)accessor).getName(), optional );
                ValueConstraintsInstance valueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    valueConstraintsInstance = valueConstraintsModel.newInstance();
                }

                // Constraints for the ManyAssociation itself
                valueConstraintsModel = constraints.constraintsFor( annotations, ManyAssociation.class, ((Member)accessor).getName(), optional );
                ValueConstraintsInstance manyValueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    manyValueConstraintsInstance = valueConstraintsModel.newInstance();
                }
                MetaInfo metaInfo = manyAssociationDeclarations.getMetaInfo( accessor );
                ManyAssociationModel associationModel = new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
                if( !mapQualifiedNameAccessor.containsKey( associationModel.qualifiedName() ) )
                {
                    manyAssociationModels.add( associationModel );
                    mapAccessorAssociationModel.put( accessor, associationModel );
                    mapQualifiedNameAccessor.put( associationModel.qualifiedName(), associationModel.accessor() );
                }
            }
            accessors.add( accessor );
        }
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

    public <T extends ManyAssociationDescriptor> Set<T> manyAssociations()
    {
        return (Set<T>) manyAssociationModels;
    }

    public <T> ManyAssociation<T> newInstance( AccessibleObject accessor, EntityState entityState, ModuleUnitOfWork uow )
    {
        return mapAccessorAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public ManyAssociationDescriptor getManyAssociationByName( String name )
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

    public ManyAssociationsInstance newInstance( EntityState entityState, ModuleUnitOfWork uow )
    {
        return new ManyAssociationsInstance( this, entityState, uow );
    }
}