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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.util.MethodKeyMap;
import org.qi4j.spi.util.MethodSet;
import org.qi4j.spi.util.MethodValueMap;

/**
 * JAVADOC
 */
public final class EntityAssociationsModel
    implements Serializable
{
    private final Set<Method> methods = new MethodSet();
    private final Set<AssociationModel> associationModels = new LinkedHashSet<AssociationModel>();
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
            if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                Annotation[] annotations = Annotations.getMethodAndTypeAnnotations( method );
                boolean optional = Annotations.getAnnotationOfType( annotations, Optional.class ) != null;

                // Constraints for Association references
                ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( annotations, GenericAssociationInfo.getAssociationType( method ), method.getName(), optional );
                ValueConstraintsInstance valueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    valueConstraintsInstance = valueConstraintsModel.newInstance();
                }

                // Constraints for the Association itself
                valueConstraintsModel = constraints.constraintsFor( annotations, Association.class, method.getName(), optional );
                ValueConstraintsInstance associationValueConstraintsInstance = null;
                if( valueConstraintsModel.isConstrained() )
                {
                    associationValueConstraintsInstance = valueConstraintsModel.newInstance();
                }

                MetaInfo metaInfo = associationDeclarations.getMetaInfo( method );
                AssociationModel associationModel = new AssociationModel( method, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
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

    public <T extends AssociationDescriptor> Set<T> associations()
    {
        return (Set<T>) associationModels;
    }

    public <T> Association<T> newInstance( Method accessor, EntityState entityState, ModuleUnitOfWork uow )
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

    public Set<AssociationType> associationTypes()
    {
        Set<AssociationType> associationTypes = new LinkedHashSet<AssociationType>();
        for( AssociationModel associationModel : associationModels )
        {
            associationTypes.add( associationModel.associationType() );
        }
        return associationTypes;
    }

    public void checkConstraints( EntityAssociationsInstance associations )
    {
        for( AssociationModel associationModel : associationModels )
        {
            associationModel.checkAssociationConstraints( associations );
            associationModel.checkConstraints( associations );
        }
    }

    public EntityAssociationsInstance newInstance( EntityState entityState, ModuleUnitOfWork uow )
    {
        return new EntityAssociationsInstance( this, entityState, uow );
    }
}
