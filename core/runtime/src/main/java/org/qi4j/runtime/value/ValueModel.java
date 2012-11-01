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

package org.qi4j.runtime.value;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.ModuleInstance;

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

    public void checkConstraints( ValueStateInstance state )
        throws ConstraintViolationException
    {
        for( PropertyModel propertyModel : stateModel.properties() )
        {
            propertyModel.checkConstraints( state.<Object>propertyFor( propertyModel.accessor() ).get() );
        }

        for( AssociationModel associationModel : ( (ValueStateModel) stateModel ).associations() )
        {
            associationModel.checkConstraints( state.<Object>associationFor( associationModel.accessor() ).get() );
        }

        for( ManyAssociationModel associationModel : ( (ValueStateModel) stateModel ).manyAssociations() )
        {
            associationModel.checkAssociationConstraints( state.<Object>manyAssociationFor( associationModel.accessor() ) );
        }
    }

    public ValueInstance newValueInstance( ModuleInstance moduleInstance,
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