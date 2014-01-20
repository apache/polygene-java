/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.value;

import java.lang.reflect.AccessibleObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.runtime.association.AssociationInfo;
import org.qi4j.runtime.association.AssociationInstance;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationInstance;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.NamedAssociationInstance;
import org.qi4j.runtime.association.NamedAssociationModel;
import org.qi4j.runtime.property.PropertyInfo;
import org.qi4j.runtime.property.PropertyInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public final class ValueStateInstance
    implements AssociationStateHolder
{
    private final Map<AccessibleObject, PropertyInstance<?>> properties;
    private final Map<AccessibleObject, AssociationInstance<?>> associations;
    private final Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations;
    private final Map<AccessibleObject, NamedAssociationInstance<?>> namedAssociations;

    public ValueStateInstance( Map<AccessibleObject, PropertyInstance<?>> properties,
                               Map<AccessibleObject, AssociationInstance<?>> associations,
                               Map<AccessibleObject, ManyAssociationInstance<?>> manyAssociations,
                               Map<AccessibleObject, NamedAssociationInstance<?>> namedAssociations
    )
    {
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
        this.namedAssociations = namedAssociations;
    }

    public ValueStateInstance( ModelModule<ValueModel> compositeModelModule,
                               ModuleInstance currentModule,
                               ValueStateModel.StateResolver stateResolver )
    {
        ValueModel valueModel = compositeModelModule.model();
        this.properties = new LinkedHashMap<>();
        for( PropertyDescriptor propertyDescriptor : valueModel.state().properties() )
        {
            PropertyInfo builderInfo = ( (PropertyModel) propertyDescriptor ).getBuilderInfo();
            Object value = stateResolver.getPropertyState( propertyDescriptor );
            PropertyInstance<Object> propertyInstance = new PropertyInstance<>( builderInfo, value );
            properties.put( propertyDescriptor.accessor(), propertyInstance );
        }

        this.associations = new LinkedHashMap<>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().associations() )
        {
            AssociationInfo builderInfo = ( (AssociationModel) associationDescriptor ).getBuilderInfo();
            EntityReference value = stateResolver.getAssociationState( associationDescriptor );
            AssociationInstance<Object> associationInstance1 = new AssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                new ReferenceProperty( value ) );
            associations.put( associationDescriptor.accessor(), associationInstance1 );
        }

        this.manyAssociations = new LinkedHashMap<>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().manyAssociations() )
        {
            AssociationInfo builderInfo = ( (ManyAssociationModel) associationDescriptor ).getBuilderInfo();
            List<EntityReference> value = stateResolver.getManyAssociationState( associationDescriptor );
            ManyAssociationValueState manyAssociationState = new ManyAssociationValueState( value );
            ManyAssociationInstance<Object> associationInstance = new ManyAssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                manyAssociationState );
            manyAssociations.put( associationDescriptor.accessor(), associationInstance );
        }

        this.namedAssociations = new LinkedHashMap<>();
        for( AssociationDescriptor associationDescriptor : valueModel.state().namedAssociations() )
        {
            AssociationInfo builderInfo = ( (NamedAssociationModel) associationDescriptor ).getBuilderInfo();
            Map<String, EntityReference> value = stateResolver.getNamedAssociationState( associationDescriptor );
            NamedAssociationValueState namedAssociationState = new NamedAssociationValueState( value );
            NamedAssociationInstance<Object> associationInstance = new NamedAssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                namedAssociationState );
            namedAssociations.put( associationDescriptor.accessor(), associationInstance );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> PropertyInstance<T> propertyFor( AccessibleObject accessor )
        throws IllegalArgumentException
    {
        PropertyInstance<T> property = (PropertyInstance<T>) properties.get( accessor );

        if( property == null )
        {
            throw new IllegalArgumentException( "No such property:" + accessor );
        }

        return property;
    }

    @Override
    public Iterable<PropertyInstance<?>> properties()
    {
        return properties.values();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> AssociationInstance<T> associationFor( AccessibleObject accessor )
    {
        AssociationInstance<T> association = (AssociationInstance<T>) associations.get( accessor );

        if( association == null )
        {
            throw new IllegalArgumentException( "No such association:" + accessor );
        }

        return association;
    }

    @Override
    public Iterable<AssociationInstance<?>> allAssociations()
    {
        return associations.values();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ManyAssociationInstance<T> manyAssociationFor( AccessibleObject accessor )
    {
        ManyAssociationInstance<T> manyAssociation = (ManyAssociationInstance<T>) manyAssociations.get( accessor );

        if( manyAssociation == null )
        {
            throw new IllegalArgumentException( "No such many-association:" + accessor );
        }

        return manyAssociation;
    }

    @Override
    public Iterable<ManyAssociationInstance<?>> allManyAssociations()
    {
        return manyAssociations.values();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> NamedAssociation<T> namedAssociationFor( AccessibleObject accessor )
    {
        NamedAssociationInstance<T> namedAssociation = (NamedAssociationInstance<T>) namedAssociations.get( accessor );

        if( namedAssociation == null )
        {
            throw new IllegalArgumentException( "No such named-association:" + accessor );
        }

        return namedAssociation;
    }

    @Override
    public Iterable<? extends NamedAssociation<?>> allNamedAssociations()
    {
        return namedAssociations.values();
    }

    @Override
    public boolean equals( Object obj )
    {
        if( !( obj instanceof ValueStateInstance ) )
        {
            return false;
        }
        ValueStateInstance state = (ValueStateInstance) obj;
        if( !properties.equals( state.properties ) )
        {
            return false;
        }
        if( !associations.equals( state.associations ) )
        {
            return false;
        }
        return manyAssociations.equals( state.manyAssociations );
    }

    @Override
    public int hashCode()
    {
        int result = properties.hashCode();
        result = 31 * result + associations.hashCode();
        result = 31 * result + manyAssociations.hashCode();
        return result;
    }
}
