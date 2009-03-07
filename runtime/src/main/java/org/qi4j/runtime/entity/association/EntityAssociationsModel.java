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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.util.MethodKeyMap;
import org.qi4j.api.util.MethodSet;
import org.qi4j.api.util.MethodValueMap;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.util.Annotations;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;

/**
 * JAVADOC
 */
public final class EntityAssociationsModel
    implements Serializable
{
    private final Set<Method> methods = new MethodSet();
    private final List<AssociationModel> associationModels = new ArrayList<AssociationModel>();
    private final Map<Method, AssociationModel> mapMethodAssociationModel = new MethodKeyMap<AssociationModel>();
    private final Map<QualifiedName, Method> accessors = new MethodValueMap<QualifiedName>();
    private final ConstraintsModel constraints;
    private AssociationDeclarations associationDeclarations;

    public EntityAssociationsModel( ConstraintsModel constraints, AssociationDeclarations associationDeclarations )
    {
        this.constraints = constraints;
        this.associationDeclarations = associationDeclarations;
    }

    public void addAssociationFor( Method method )
    {
        if( !methods.contains( method ) )
        {
            if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                Annotation[] annotations = Annotations.getMethodAndTypeAnnotations( method );
                boolean optional = Annotations.getAnnotationOfType( annotations, Optional.class ) != null;
                ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericAssociationInfo.getAssociationType( method ), method.getName(), optional );
                ValueConstraintsInstance valueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    valueConstraintsInstance = valueConstraintsModel.newInstance();
                }
                MetaInfo metaInfo = associationDeclarations.getMetaInfo( method );
                AssociationModel associationModel = new AssociationModel( method, valueConstraintsInstance, metaInfo );
                if( !accessors.containsKey( associationModel.qualifiedName() ) )
                {
                    associationModels.add( associationModel );
                    mapMethodAssociationModel.put( method, associationModel );
                    accessors.put( associationModel.qualifiedName(), associationModel.accessor() );
                }
            }
            methods.add( method );
        }
    }

    public List<AssociationDescriptor> associations()
    {
        return new ArrayList<AssociationDescriptor>( associationModels );
    }

    public AssociationsInstance newInstance( ModuleUnitOfWork uow, EntityState state )
    {
        Map<Method, AbstractAssociation> associations = new MethodKeyMap<AbstractAssociation>();
        for( AssociationModel associationModel : associationModels )
        {
            AbstractAssociation association = associationModel.newInstance( uow, state );
            associations.put( associationModel.accessor(), association );
        }

        return new AssociationsInstance( this, uow, state );
    }

    public AssociationsInstance newBuilderInstance()
    {
        return new AssociationsInstance( this, null, null );
    }

    public AbstractAssociation newDefaultInstance( Method accessor )
    {
        return mapMethodAssociationModel.get( accessor ).newDefaultInstance();
    }

    public AbstractAssociation newInstance( Method accessor, EntityState entityState, ModuleUnitOfWork uow )
    {
        return mapMethodAssociationModel.get( accessor ).newInstance( uow, entityState );
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

    public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
    {
        for( AssociationModel associationModel : associationModels )
        {
            if( associationModel.qualifiedName().equals( name ) )
            {
                return associationModel;
            }
        }

        return null;
    }

    public void setState( AssociationsInstance associations, EntityState entityState )
    {
        for( Map.Entry<Method, AssociationModel> methodAssociationModelEntry : mapMethodAssociationModel.entrySet() )
        {
            AbstractAssociation association = associations.associationFor( methodAssociationModelEntry.getKey() );
            methodAssociationModelEntry.getValue().setState( association, entityState );
        }
    }

    public Iterable<AssociationType> associationTypes()
    {
        List<AssociationType> associationTypes = new ArrayList<AssociationType>();
        for( AssociationModel associationModel : associationModels )
        {
            if( associationModel.isAssociation() )
            {
                associationTypes.add( associationModel.associationType() );
            }
        }
        return associationTypes;
    }

    public Iterable<ManyAssociationType> manyAssociationTypes()
    {
        List<ManyAssociationType> associationTypes = new ArrayList<ManyAssociationType>();
        for( AssociationModel associationModel : associationModels )
        {
            if( !associationModel.isAssociation() )
            {
                associationTypes.add( associationModel.manyAssociationType() );
            }
        }
        return associationTypes;
    }
}
