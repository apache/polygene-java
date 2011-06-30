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
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.structure.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for ValueComposites
 */
public final class ValueModel
    extends AbstractCompositeModel
    implements ValueDescriptor
{
    private ValueCompositeType valueType;

    public static ValueModel newModel( final Class<? extends ValueComposite> compositeType,
                                       final Visibility visibility,
                                       final MetaInfo metaInfo,
                                       final PropertyDeclarations propertyDeclarations,
                                       final List<Class<?>> assemblyConcerns,
                                       final List<Class<?>> sideEffects,
                                       final List<Class<?>> mixins,
                                       final List<Class<?>> roles,
                                       AssemblyHelper helper )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        ValuePropertiesModel propertiesModel = new ValuePropertiesModel( constraintsModel, propertyDeclarations );

        ValueStateModel stateModel = new ValueStateModel( propertiesModel );
        ValueMixinsModel mixinsModel = new ValueMixinsModel( compositeType, roles, mixins );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( assemblyConcerns, concerns );
        ConcernsDeclaration.concernDeclarations( compositeType, concerns );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( concerns );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType, sideEffects );
        // TODO: Disable constraints, concerns and sideeffects??
        CompositeMethodsModel compositeMethodsModel =
            new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel, helper );
        stateModel.addStateFor( compositeMethodsModel.methods(), mixinsModel );

        return new ValueModel( compositeType, roles, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    private ValueModel( final Class<? extends ValueComposite> compositeType,
                        final List<Class<?>> roles,
                        final Visibility visibility,
                        final MetaInfo metaInfo,
                        final ValueMixinsModel mixinsModel,
                        final ValueStateModel stateModel,
                        final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( compositeType, roles, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        valueType = new ValueCompositeType( this );
    }

    public ValueCompositeType valueType()
    {
        return valueType;
    }

    public void checkConstraints( StateHolder state )
        throws ConstraintViolationException
    {
        stateModel.checkConstraints( state );
    }

    public ValueInstance newValueInstance( ModuleInstance moduleInstance,
                                           StateHolder state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        ValueInstance instance = new ValueInstance( this, moduleInstance, mixins, state );

        try
        {
            // Instantiate all mixins
            ( (ValueMixinsModel) mixinsModel ).newMixins( instance,
                                                          state,
                                                          mixins );
        }
        catch( InvalidCompositeException e )
        {
            e.setFailingCompositeType( type() );
            e.setMessage( "Invalid Cyclic Mixin usage dependency" );
            throw e;
        }
        // Return
        return instance;
    }
}