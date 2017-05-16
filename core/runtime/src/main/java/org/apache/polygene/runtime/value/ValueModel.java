/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.value;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.runtime.composite.CompositeMethodsModel;
import org.apache.polygene.runtime.composite.CompositeModel;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.MixinsModel;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.unitofwork.UnitOfWorkInstance;

/**
 * Model for ValueComposites
 */
public final class ValueModel extends CompositeModel
    implements ValueDescriptor
{
    private ValueCompositeType valueType;

    public ValueModel( final ModuleDescriptor module,
                       final List<Class<?>> types,
                       final Visibility visibility,
                       final MetaInfo metaInfo,
                       final MixinsModel mixinsModel,
                       final ValueStateModel stateModel,
                       final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( module, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        valueType = ValueCompositeType.of( this );
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
        stateModel.properties().forEach(
            propertyModel ->
            {
                try
                {
                    propertyModel.checkConstraints( state.propertyFor( propertyModel.accessor() ).get() );
                }
                catch( ConstraintViolationException e )
                {
                    throw new ConstraintViolationException( "<builder>", propertyModel.valueType()
                        .types(), (Member) propertyModel.accessor(), e.constraintViolations() );
                }
            }
        );

        // IF no UnitOfWork is active, then the Association checks shouldn't be done.
        if( UnitOfWorkInstance.getCurrent().empty() )
        {
            return;
        }
        ( (ValueStateModel) stateModel ).associations().forEach(
            associationModel ->
            {
                try
                {
                    associationModel.checkConstraints( state.associationFor( associationModel.accessor() ).get() );
                }
                catch( ConstraintViolationException e )
                {
                    Stream<? extends Type> types = Classes.interfacesOf( associationModel.type() );
                    throw new ConstraintViolationException( "<builder>", types,
                                                            (Member) associationModel.accessor(),
                                                            e.constraintViolations() );
                }
            }
        );

        ( (ValueStateModel) stateModel ).manyAssociations().forEach(
            model -> model.checkAssociationConstraints( state.manyAssociationFor( model.accessor() ) )
        );

        ( (ValueStateModel) stateModel ).namedAssociations().forEach(
            model -> model.checkAssociationConstraints( state.namedAssociationFor( model.accessor() ) )
        );
    }

    public ValueInstance newValueInstance( ValueStateInstance state )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        ValueInstance instance = new ValueInstance( this, mixins, state );

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