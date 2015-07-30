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

package org.apache.zest.runtime.value;

import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.CompositeModel;
import org.apache.zest.runtime.composite.MixinModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.composite.UsesInstance;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.runtime.unitofwork.UnitOfWorkInstance;
import org.apache.zest.spi.module.ModuleSpi;

/**
 * Model for ValueComposites
 */
public final class ValueModel
    extends CompositeModel
    implements ValueDescriptor
{
    private ValueCompositeType valueType;

    public ValueModel( final Iterable<Class<?>> types,
                       final Visibility visibility,
                       final MetaInfo metaInfo,
                       final MixinsModel mixinsModel,
                       final ValueStateModel stateModel,
                       final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        valueType = new ValueCompositeType( this );
    }

    @Override
    public ValueCompositeType valueType()
    {
        return valueType;
    }

    @Override
    public ValueStateModel state()
    {
        return (ValueStateModel) super.state();
    }

    // This method is ONLY called by ValueBuilders
    void checkConstraints( ValueStateInstance state )
        throws ConstraintViolationException
    {
        for( PropertyModel propertyModel : stateModel.properties() )
        {
            propertyModel.checkConstraints( state.propertyFor( propertyModel.accessor() ).get() );
        }

        // IF no UnitOfWork is active, then the Association checks shouldn't be done.
        if( UnitOfWorkInstance.getCurrent().empty() )
        {
            return;
        }
        for( AssociationModel associationModel : ( (ValueStateModel) stateModel ).associations() )
        {
            associationModel.checkConstraints( state.associationFor( associationModel.accessor() ).get() );
        }

        for( ManyAssociationModel associationModel : ( (ValueStateModel) stateModel ).manyAssociations() )
        {
            associationModel.checkAssociationConstraints( state.manyAssociationFor( associationModel.accessor() ) );
        }
    }

    public ValueInstance newValueInstance( ModuleSpi moduleInstance,
                                           ValueStateInstance state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        ValueInstance instance = new ValueInstance( this, moduleInstance, mixins, state );

        // Instantiate all mixins
        int i = 0;
        InjectionContext injectionContext = new InjectionContext( instance, UsesInstance.EMPTY_USES, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        // Return
        return instance;
    }
}