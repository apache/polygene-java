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

import org.qi4j.api.entity.EntityStateDescriptor;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.composite.AbstractStateModel;
import org.qi4j.runtime.entity.association.*;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.AccessibleObject;
import java.util.Set;

/**
 * JAVADOC
 */
public final class EntityStateModel
    extends AbstractStateModel<EntityPropertiesModel>
    implements EntityStateDescriptor
{
    private final AssociationsModel associationsModel;
    private final ManyAssociationsModel manyAssociationsModel;

    public EntityStateModel( EntityPropertiesModel propertiesModel,
                             AssociationsModel associationsModel,
                             ManyAssociationsModel manyAssociationsModel
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

    public AssociationDescriptor getAssociationByName( String name )
    {
        return associationsModel.getAssociationByName( name );
    }

    public AssociationDescriptor getManyAssociationByName( String name )
    {
        return manyAssociationsModel.getManyAssociationByName( name );
    }

    public <T extends AssociationDescriptor> Set<T> associations()
    {
        return associationsModel.associations();
    }

    public <T extends AssociationDescriptor> Set<T> manyAssociations()
    {
        return manyAssociationsModel.manyAssociations();
    }

    protected void addStateFor(AccessibleObject accessor)
    {
        Class<?> stateType = Classes.RAW_CLASS.map( Classes.TYPE_OF.map( accessor ) );
        if( Association.class.isAssignableFrom( stateType ))
        {
            associationsModel.addAssociationFor( accessor );
        }
        else if( ManyAssociation.class.isAssignableFrom( stateType ))
        {
            manyAssociationsModel.addManyAssociationFor( accessor );
        } else
            super.addStateFor(accessor);
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if (visitor.visitEnter( this ))
        {
            if (((VisitableHierarchy<Object, Object>)propertiesModel).accept(visitor))
                if (((VisitableHierarchy<AssociationsModel, AssociationModel >)associationsModel).accept(visitor))
                    ((VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>)manyAssociationsModel).accept(visitor);

        }

        return visitor.visitLeave( this );
    }

    public final class EntityStateInstance
        implements EntityStateHolder
    {
        private final EntityPropertiesInstance entityPropertiesInstance;
        private final AssociationsInstance associationsInstance;
        private final ManyAssociationsInstance manyAssociationsInstance;

        private EntityStateInstance(
            EntityPropertiesInstance entityPropertiesInstance,
            AssociationsInstance associationsInstance,
            ManyAssociationsInstance manyAssociationsInstance
        )
        {
            this.entityPropertiesInstance = entityPropertiesInstance;
            this.associationsInstance = associationsInstance;
            this.manyAssociationsInstance = manyAssociationsInstance;
        }

        public <T> Property<T> getProperty( AccessibleObject accessor )
                throws IllegalArgumentException
        {
            return entityPropertiesInstance.<T>getProperty( accessor );
        }

        @Override
        public Iterable<Property<?>> properties()
        {
            return entityPropertiesInstance.properties();
        }

        public <T> Association<T> getAssociation( AccessibleObject accessor )
        {
            return associationsInstance.associationFor( accessor );
        }

        public <T> ManyAssociation<T> getManyAssociation( AccessibleObject accessor )
        {
            return manyAssociationsInstance.manyAssociationFor( accessor );
        }

        @Override
        public Iterable<Association<?>> associations()
        {
            return associationsInstance.associations();
        }

        @Override
        public Iterable<ManyAssociation<?>> manyAssociations()
        {
            return manyAssociationsInstance.manyAssociations();
        }

        public void checkConstraints()
        {
            entityPropertiesInstance.checkConstraints();
            associationsInstance.checkConstraints();
            manyAssociationsInstance.checkConstraints();
        }
    }
}
