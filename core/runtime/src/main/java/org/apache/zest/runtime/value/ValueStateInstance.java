/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012, Kent Sølvsten. All Rights Reserved.
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
package org.apache.zest.runtime.value;

import java.lang.reflect.AccessibleObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.runtime.association.AssociationInfo;
import org.apache.zest.runtime.association.AssociationInstance;
import org.apache.zest.runtime.association.ManyAssociationInstance;
import org.apache.zest.runtime.association.NamedAssociationInstance;
import org.apache.zest.runtime.composite.StateResolver;
import org.apache.zest.runtime.property.PropertyInfo;
import org.apache.zest.runtime.property.PropertyInstance;
import org.apache.zest.runtime.structure.ModuleInstance;
import org.apache.zest.spi.module.ModelModule;

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

    public ValueStateInstance( ModelModule<ValueDescriptor> compositeModelModule,
                               ModuleInstance currentModule,
                               StateResolver stateResolver
    )
    {
        ValueModel valueModel = (ValueModel) compositeModelModule.model();
        this.properties = new LinkedHashMap<>();
        valueModel.state().properties().forEach( propertyDescriptor -> {
            PropertyInfo builderInfo = propertyDescriptor.getBuilderInfo();
            Object value = stateResolver.getPropertyState( propertyDescriptor );
            PropertyInstance<Object> propertyInstance = new PropertyInstance<>( builderInfo, value );
            properties.put( propertyDescriptor.accessor(), propertyInstance );
        } );

        this.associations = new LinkedHashMap<>();
        valueModel.state().associations().forEach( associationDescriptor -> {
            AssociationInfo builderInfo = associationDescriptor.getBuilderInfo();
            EntityReference value = stateResolver.getAssociationState( associationDescriptor );
            AssociationInstance<Object> associationInstance1 = new AssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                new ReferenceProperty( value ) );
            associations.put( associationDescriptor.accessor(), associationInstance1 );
        } );

        this.manyAssociations = new LinkedHashMap<>();
        valueModel.state().manyAssociations().forEach( associationDescriptor -> {
            AssociationInfo builderInfo = associationDescriptor
                .getBuilderInfo();
            List<EntityReference> value = stateResolver.getManyAssociationState( associationDescriptor );
            ManyAssociationValueState manyAssociationState = new ManyAssociationValueState( value );
            ManyAssociationInstance<Object> associationInstance = new ManyAssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                manyAssociationState );
            manyAssociations.put( associationDescriptor.accessor(), associationInstance );
        } );

        this.namedAssociations = new LinkedHashMap<>();
        valueModel.state().namedAssociations().forEach( associationDescriptor -> {
            AssociationInfo builderInfo = associationDescriptor
                .getBuilderInfo();
            Map<String, EntityReference> value = stateResolver.getNamedAssociationState( associationDescriptor );
            NamedAssociationValueState namedAssociationState = new NamedAssociationValueState( value );
            NamedAssociationInstance<Object> associationInstance = new NamedAssociationInstance<>(
                builderInfo,
                currentModule.getEntityFunction(),
                namedAssociationState );
            namedAssociations.put( associationDescriptor.accessor(), associationInstance );
        } );
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
    public Stream<PropertyInstance<?>> properties()
    {
        return properties.values().stream();
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
    public Stream<AssociationInstance<?>> allAssociations()
    {
        return associations.values().stream();
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
    public Stream<ManyAssociationInstance<?>> allManyAssociations()
    {
        return manyAssociations.values().stream();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> NamedAssociationInstance<T> namedAssociationFor( AccessibleObject accessor )
    {
        NamedAssociationInstance<T> namedAssociation = (NamedAssociationInstance<T>) namedAssociations.get( accessor );

        if( namedAssociation == null )
        {
            throw new IllegalArgumentException( "No such named-association:" + accessor );
        }

        return namedAssociation;
    }

    @Override
    public Stream<? extends NamedAssociationInstance<?>> allNamedAssociations()
    {
        return namedAssociations.values().stream();
    }

    @SuppressWarnings( "SimplifiableIfStatement" )
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
        if( !manyAssociations.equals( state.manyAssociations ) )
        {
            return false;
        }
        return namedAssociations.equals( state.namedAssociations );
    }

    @Override
    public int hashCode()
    {
        int result = properties.hashCode();
        result = 31 * result + associations.hashCode();
        result = 31 * result + manyAssociations.hashCode();
        result = 31 * result + namedAssociations.hashCode();
        return result;
    }
}
