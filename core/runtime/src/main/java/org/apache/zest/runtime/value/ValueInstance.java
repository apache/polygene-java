/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.value;

import java.lang.reflect.Proxy;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.composite.MixinsInstance;
import org.apache.zest.runtime.composite.TransientInstance;
import org.apache.zest.runtime.property.PropertyInstance;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.spi.module.ModuleSpi;

/**
 * ValueComposite instance
 */
public final class ValueInstance
    extends TransientInstance
    implements CompositeInstance, MixinsInstance
{
    public static ValueInstance valueInstanceOf( ValueComposite composite )
    {
        return (ValueInstance) Proxy.getInvocationHandler( composite );
    }

    public ValueInstance( ValueModel compositeModel,
                          Object[] mixins,
                          ValueStateInstance state
    )
    {
        super( compositeModel, mixins, state );
    }

    /**
     * Perform equals with {@code o} argument.
     * <p>
     * The definition of equals() for the Value is that if both the state and descriptor are equal,
     * then the values are equal.
     * </p>
     *
     * @param o The other object to compare.
     *
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
     */
    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || !Proxy.isProxyClass( o.getClass() ) )
        {
            return false;
        }

        try
        {
            ValueInstance that = (ValueInstance) Proxy.getInvocationHandler( o );
            // Descriptor equality
            if( !descriptor().equals( that.descriptor() ) )
            {
                return false;
            }
            // State equality
            return state.equals( that.state );
        }
        catch( ClassCastException e )
        {
            return false;
        }
    }

    @Override
    public ValueStateInstance state()
    {
        return (ValueStateInstance) state;
    }

    @Override
    public ValueModel descriptor()
    {
        return (ValueModel) compositeModel;
    }

    /**
     * When a ValueBuilder is about to start, ensure that all state has builder infos, i.e. they are mutable.
     */
    public void prepareToBuild()
    {
        descriptor().state().properties().forEach( propertyDescriptor -> {
            PropertyInstance<Object> propertyInstance =
                (PropertyInstance<Object>) state.propertyFor( propertyDescriptor.accessor() );

            propertyInstance.prepareToBuild( propertyDescriptor );
        } );

        descriptor().state().associations().forEach( associationDescriptor -> {
            state().associationFor( associationDescriptor.accessor() )
                .setAssociationInfo( associationDescriptor.getBuilderInfo() );
        } );

        descriptor().state().manyAssociations().forEach( associationDescriptor -> {
            state().manyAssociationFor( associationDescriptor.accessor() )
                .setAssociationInfo( associationDescriptor.getBuilderInfo() );
        } );

        descriptor().state().namedAssociations().forEach( associationDescriptor -> {
            state().namedAssociationFor( associationDescriptor.accessor() )
                .setAssociationInfo( associationDescriptor.getBuilderInfo() );
        } );
    }

    /**
     * When a ValueBuilder is finished and is about to instantiate a Value, call this to ensure that the state has correct
     * settings, i.e. is immutable.
     */
    public void prepareBuilderState()
    {
        descriptor().state().properties().forEach( propertyDescriptor -> {
            PropertyInstance<Object> propertyInstance =
                (PropertyInstance<Object>) state.propertyFor( propertyDescriptor.accessor() );
            propertyInstance.prepareBuilderState( propertyDescriptor );
        } );

        descriptor().state().associations().forEach( associationDescriptor -> {
            state().associationFor( associationDescriptor.accessor() ).setAssociationInfo( associationDescriptor );
        } );

        descriptor().state().manyAssociations().forEach( associationDescriptor -> {
            state().manyAssociationFor( associationDescriptor.accessor() ).setAssociationInfo( associationDescriptor );
        } );

        descriptor().state().namedAssociations().forEach( associationDescriptor -> {
            state().namedAssociationFor( associationDescriptor.accessor() ).setAssociationInfo( associationDescriptor );
        } );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this instance.
     */
    @Override
    public int hashCode()
    {
        int hash = compositeModel.hashCode() * 23; // Descriptor
        return hash + state.hashCode() * 5; // State
    }

    @Override
    public String toString()
    {
        return ( (ModuleSpi) module().instance() ).valueSerialization().serialize( this.<ValueComposite>proxy() );
    }
}
