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

package org.qi4j.runtime.composite;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.qi4j.composite.State;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.property.Property;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;

/**
 * TODO
 */
public class StateModel
    implements StateDescriptor
{
    private PropertiesModel propertiesModel;

    public StateModel( PropertiesModel propertiesModel )
    {
        this.propertiesModel = propertiesModel;
    }

    public State newDefaultInstance()
    {
        PropertiesInstance properties = propertiesModel.newDefaultInstance();
        return new StateInstance( properties );
    }

    public State newInstance( State propertiesState )
    {
        PropertiesInstance properties = propertiesModel.newInstance( propertiesState );
        return new StateInstance( properties );
    }

    public void addStateFor( Class mixinType )
    {
        propertiesModel.addPropertiesFor( mixinType );
    }

    public PropertyDescriptor getPropertyByName( String name )
    {
        return propertiesModel.getPropertyByName( name );
    }

    public PropertyDescriptor getPropertyByQualifiedName( String name )
    {
        return propertiesModel.getPropertyByQualifiedName( name );
    }


    public AssociationDescriptor getAssociationByName( String name )
    {
        return null;
    }

    public AssociationDescriptor getAssociationByQualifiedName( String name )
    {
        return null;
    }

    public List<PropertyDescriptor> properties()
    {
        return propertiesModel.properties();
    }

    public List<AssociationDescriptor> associations()
    {
        return Collections.EMPTY_LIST;
    }

    private static final class StateInstance
        implements State
    {
        private PropertiesInstance properties;

        private StateInstance( PropertiesInstance properties )
        {
            this.properties = properties;
        }

        public Property<?> getProperty( Method propertyMethod )
        {
            return properties.propertyFor( propertyMethod );
        }

        public AbstractAssociation getAssociation( Method associationMethod )
        {
            return null;
        }
    }
}
