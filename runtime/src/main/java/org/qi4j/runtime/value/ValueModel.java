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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.AbstractCompositeModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernDeclaration;
import org.qi4j.runtime.composite.ConcernsDeclaration;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.SideEffectsDeclaration;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.types.ValueCompositeType;
import org.qi4j.runtime.types.ValueTypeFactory;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * Model for ValueComposites
 */
public final class ValueModel
    extends AbstractCompositeModel
    implements ValueDescriptor, Serializable
{
    private ValueCompositeType valueType;

    public static ValueModel newModel( final Class<? extends ValueComposite> compositeType,
                                       final Visibility visibility,
                                       final MetaInfo metaInfo,
                                       final PropertyDeclarations propertyDeclarations,
                                       final List<Class<?>> assemblyConcerns,
                                       final List<Class<?>> sideEffects,
                                       final List<Class<?>> mixins
    )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        ValuePropertiesModel propertiesModel = new ValuePropertiesModel( constraintsModel, propertyDeclarations );

        ValueStateModel stateModel = new ValueStateModel( propertiesModel );
        ValueMixinsModel mixinsModel = new ValueMixinsModel( compositeType, mixins );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( assemblyConcerns, concerns );
        ConcernsDeclaration.concernDeclarations( compositeType, concerns );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( concerns );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType, sideEffects );
        // TODO: Disable constraints, concerns and sideeffects??
        CompositeMethodsModel compositeMethodsModel =
            new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel );
        stateModel.addStateFor( compositeMethodsModel.methods(), compositeType );

        ValueCompositeType valueType = (ValueCompositeType) ValueTypeFactory.instance()
            .newValueType( compositeType, compositeType, compositeType );

        return new ValueModel( compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel, valueType );
    }

    private ValueModel( final Class<? extends ValueComposite> compositeType,
                        final Visibility visibility,
                        final MetaInfo metaInfo,
                        final ValueMixinsModel mixinsModel,
                        final ValueStateModel stateModel,
                        final CompositeMethodsModel compositeMethodsModel,
                        ValueCompositeType valueType
    )
    {
        super( compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        this.valueType = valueType;
    }

    public ValueCompositeType valueType()
    {
        return valueType;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositeMethodsModel.visitModel( modelVisitor );
        mixinsModel.visitModel( modelVisitor );
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
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