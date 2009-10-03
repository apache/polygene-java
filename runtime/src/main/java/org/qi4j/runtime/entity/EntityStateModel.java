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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.util.Set;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.composite.AbstractStateModel;
import org.qi4j.runtime.entity.association.EntityAssociationsInstance;
import org.qi4j.runtime.entity.association.EntityAssociationsModel;
import org.qi4j.runtime.entity.association.EntityManyAssociationsInstance;
import org.qi4j.runtime.entity.association.EntityManyAssociationsModel;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStateDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

/**
 * JAVADOC
 */
public final class EntityStateModel
    extends AbstractStateModel<EntityPropertiesModel>
    implements EntityStateDescriptor
{
    private final EntityAssociationsModel associationsModel;
    private EntityManyAssociationsModel manyAssociationsModel;

    public EntityStateModel( EntityPropertiesModel propertiesModel,
                             EntityAssociationsModel associationsModel,
                             EntityManyAssociationsModel manyAssociationsModel
    )
    {
        super( propertiesModel );
        this.associationsModel = associationsModel;
        this.manyAssociationsModel = manyAssociationsModel;
    }

    public EntityStateModel.EntityStateInstance newInstance( ModuleUnitOfWork uow, EntityState entityState )
    {
        return new EntityStateInstance( propertiesModel.newInstance( entityState ),
                                        associationsModel.newInstance( entityState, uow ),
                                        manyAssociationsModel.newInstance( entityState, uow ) );
    }

    @Override
    public void addStateFor( Iterable<Method> methods, Class compositeType )
    {
        super.addStateFor( methods, compositeType );
        for( Method method : methods )
        {
            associationsModel.addAssociationFor( method );
        }
        for( Method method : methods )
        {
            manyAssociationsModel.addManyAssociationFor( method );
        }
    }

    public AssociationDescriptor getAssociationByName( String name )
    {
        return associationsModel.getAssociationByName( name );
    }

    public ManyAssociationDescriptor getManyAssociationByName( String name )
    {
        return manyAssociationsModel.getManyAssociationByName( name );
    }

    public <T extends AssociationDescriptor> Set<T> associations()
    {
        return associationsModel.associations();
    }

    public <T extends ManyAssociationDescriptor> Set<T> manyAssociations()
    {
        return manyAssociationsModel.manyAssociations();
    }

    public Set<PropertyType> propertyTypes()
    {
        return propertiesModel.propertyTypes();
    }

    public Set<AssociationType> associationTypes()
    {
        return associationsModel.associationTypes();
    }

    public Set<ManyAssociationType> manyAssociationTypes()
    {
        return manyAssociationsModel.manyAssociationTypes();
    }

    public final class EntityStateInstance
        implements EntityStateHolder
    {
        private final EntityPropertiesInstance entityPropertiesInstance;
        private final EntityAssociationsInstance entityAssociationsInstance;
        private final EntityManyAssociationsInstance entityManyAssociationsInstance;

        private EntityStateInstance(
            EntityPropertiesInstance entityPropertiesInstance,
            EntityAssociationsInstance entityAssociationsInstance,
            EntityManyAssociationsInstance entityManyAssociationsInstance
        )
        {
            this.entityPropertiesInstance = entityPropertiesInstance;
            this.entityAssociationsInstance = entityAssociationsInstance;
            this.entityManyAssociationsInstance = entityManyAssociationsInstance;
        }

        public <T> Property<T> getProperty( Method accessor )
        {
            return entityPropertiesInstance.<T>getProperty( accessor );
        }

        public <T> Property<T> getProperty( QualifiedName name )
        {
            return entityPropertiesInstance.getProperty( name );
        }

        public <T> Association<T> getAssociation( Method accessor )
        {
            return entityAssociationsInstance.associationFor( accessor );
        }

        public <T> ManyAssociation<T> getManyAssociation( Method accessor )
        {
            return entityManyAssociationsInstance.manyAssociationFor( accessor );
        }

        public void visitState( EntityStateVisitor visitor )
        {
            visitProperties( visitor );

            entityAssociationsInstance.visitAssociations( visitor );
            entityManyAssociationsInstance.visitManyAssociations( visitor );
        }

        public void visitProperties( StateVisitor visitor )
        {
            entityPropertiesInstance.visitProperties( visitor );
        }

        public void checkConstraints()
        {
            entityPropertiesInstance.checkConstraints();
            entityAssociationsInstance.checkConstraints();
            entityManyAssociationsInstance.checkConstraints();
        }
    }
}
