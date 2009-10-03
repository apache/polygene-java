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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.util.MethodKeyMap;
import org.qi4j.spi.util.MethodSet;
import org.qi4j.spi.util.MethodValueMap;

/**
 * JAVADOC
 */
public final class EntityManyAssociationsModel
    implements Serializable
{
    private final Set<Method> methods = new MethodSet();
    private final Set<ManyAssociationModel> manyAssociationModels = new LinkedHashSet<ManyAssociationModel>();
    private final Map<Method, ManyAssociationModel> mapMethodAssociationModel = new MethodKeyMap<ManyAssociationModel>();
    private final Map<QualifiedName, Method> accessors = new MethodValueMap<QualifiedName>();
    private final ConstraintsModel constraints;
    private final ManyAssociationDeclarations manyAssociationDeclarations;

    public EntityManyAssociationsModel( ConstraintsModel constraints,
                                        ManyAssociationDeclarations manyAssociationDeclarations
    )
    {
        this.constraints = constraints;
        this.manyAssociationDeclarations = manyAssociationDeclarations;
    }

    public void addManyAssociationFor( Method method )
    {
        if( !methods.contains( method ) )
        {
            if( ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                Annotation[] annotations = Annotations.getMethodAndTypeAnnotations( method );
                boolean optional = Annotations.getAnnotationOfType( annotations, Optional.class ) != null;

                // Constraints for entities in ManyAssociation
                ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericAssociationInfo.getAssociationType( method ), method.getName(), optional );
                ValueConstraintsInstance valueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    valueConstraintsInstance = valueConstraintsModel.newInstance();
                }

                // Constraints for the ManyAssociation itself
                valueConstraintsModel = constraints.constraintsFor( annotations, ManyAssociation.class, method.getName(), optional );
                ValueConstraintsInstance manyValueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    manyValueConstraintsInstance = valueConstraintsModel.newInstance();
                }
                MetaInfo metaInfo = manyAssociationDeclarations.getMetaInfo( method );
                ManyAssociationModel associationModel = new ManyAssociationModel( method, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
                if( !accessors.containsKey( associationModel.qualifiedName() ) )
                {
                    manyAssociationModels.add( associationModel );
                    mapMethodAssociationModel.put( method, associationModel );
                    accessors.put( associationModel.qualifiedName(), associationModel.accessor() );
                }
            }
            methods.add( method );
        }
    }

    public <T extends ManyAssociationDescriptor> Set<T> manyAssociations()
    {
        return (Set<T>) manyAssociationModels;
    }

    public <T> ManyAssociation<T> newInstance( Method accessor, EntityState entityState, ModuleUnitOfWork uow )
    {
        return mapMethodAssociationModel.get( accessor ).newInstance( uow, entityState );
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

    public Set<ManyAssociationType> manyAssociationTypes()
    {
        Set<ManyAssociationType> associationTypes = new LinkedHashSet<ManyAssociationType>();
        for( ManyAssociationModel associationModel : manyAssociationModels )
        {
            associationTypes.add( associationModel.manyAssociationType() );
        }
        return associationTypes;
    }

    public void checkConstraints( EntityManyAssociationsInstance entityManyAssociationsInstance )
    {
        for( ManyAssociationModel manyAssociationModel : manyAssociationModels )
        {
            ManyAssociation manyAssociation = entityManyAssociationsInstance.manyAssociationFor( manyAssociationModel.accessor() );
            manyAssociationModel.checkAssociationConstraints( manyAssociation );
        }
    }

    public EntityManyAssociationsInstance newInstance( EntityState entityState, ModuleUnitOfWork uow )
    {
        return new EntityManyAssociationsInstance( this, entityState, uow );
    }
}