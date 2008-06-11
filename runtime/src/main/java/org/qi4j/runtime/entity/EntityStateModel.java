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
import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.composite.State;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.entity.association.AssociationsInstance;
import org.qi4j.runtime.entity.association.AssociationsModel;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.structure.AssociationDescriptor;
import org.qi4j.spi.structure.PropertyDescriptor;

/**
 * TODO
 */
public class EntityStateModel
    implements StateDescriptor
{
    private EntityPropertiesModel propertiesModel;
    private AssociationsModel associationsModel;

    public EntityStateModel( EntityPropertiesModel propertiesModel, AssociationsModel associationsModel )
    {
        this.propertiesModel = propertiesModel;
        this.associationsModel = associationsModel;
    }

    public State newDefaultInstance()
    {
        PropertiesInstance properties = propertiesModel.newDefaultInstance();
        AssociationsInstance associations = associationsModel.newDefaultInstance();
        return new StateInstance( properties, associations );
    }

    public State newInstance( UnitOfWorkInstance uow, EntityState entityState )
    {
        return new EntityStateInstance( propertiesModel, associationsModel, entityState, uow );
    }

    public void addStateFor( Class mixinType )
    {
        propertiesModel.addPropertiesFor( mixinType );
        associationsModel.addAssociationsFor( mixinType );
    }

    public PropertyDescriptor getPropertyByName( String name )
    {
        return propertiesModel.getPropertyByName( name );
    }

    public AssociationDescriptor getAssociationByName( String name )
    {
        return associationsModel.getAssociationByName( name );
    }

    public void setState( State state, EntityState entityState )
        throws ConstraintViolationException
    {
        StateInstance stateInstance = (StateInstance) state;

        propertiesModel.setState( stateInstance.properties, entityState );
        associationsModel.setState( stateInstance.associations, entityState );
    }

    private static final class StateInstance
        implements State
    {
        private PropertiesInstance properties;
        private AssociationsInstance associations;

        private StateInstance( PropertiesInstance properties, AssociationsInstance associations )
        {
            this.properties = properties;
            this.associations = associations;
        }

        public Property<?> getProperty( Method propertyMethod )
        {
            return properties.propertyFor( propertyMethod );
        }

        public AbstractAssociation getAssociation( Method associationMethod )
        {
            return associations.associationFor( associationMethod );
        }
    }

    private static final class EntityStateInstance
        implements State
    {
        private Map<Method, Property> properties;
        private Map<Method, AbstractAssociation> associations;

        private EntityPropertiesModel entityPropertiesModel;
        private AssociationsModel associationsModel;
        private EntityState entityState;
        private UnitOfWorkInstance uow;

        private EntityStateInstance( EntityPropertiesModel entityPropertiesModel, AssociationsModel associationsModel, EntityState entityState, UnitOfWorkInstance uow )
        {
            this.entityPropertiesModel = entityPropertiesModel;
            this.associationsModel = associationsModel;
            this.entityState = entityState;
            this.uow = uow;
        }

        public Property<?> getProperty( Method accessor )
        {
            if( properties == null )
            {
                properties = new HashMap<Method, Property>();
            }

            Property<?> property = properties.get( accessor );

            if( property == null )
            {
                property = entityPropertiesModel.newInstance( accessor, entityState );
                properties.put( accessor, property );
            }

            return property;
        }

        public AbstractAssociation getAssociation( Method accessor )
        {
            if( associations == null )
            {
                associations = new HashMap<Method, AbstractAssociation>();
            }

            AbstractAssociation association = associations.get( accessor );

            if( association == null )
            {
                association = associationsModel.newInstance( accessor, entityState, uow );
                associations.put( accessor, association );
            }

            return association;
        }
    }

}
