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

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.constraint.ValueConstraintViolation;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.structure.TypeLookup;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.unitofwork.NoSuchEntityTypeException;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.runtime.composite.CompositeMethodsModel;
import org.apache.polygene.runtime.composite.CompositeModel;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.MixinsModel;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.property.PropertyInstance;
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
// TODO: When TypeLookup's lazy loading can be disabled during Model building, then uncomment the following line.
//        checkAssociationVisibility();
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
        List<ValueConstraintViolation> violations = new ArrayList<>();

        stateModel.properties().forEach(
            propertyModel ->
            {
                try
                {
                    propertyModel.checkConstraints( state.propertyFor( propertyModel.accessor() ).get() );
                }
                catch( ConstraintViolationException e )
                {
                    violations.addAll( e.constraintViolations() );
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
                    violations.addAll( e.constraintViolations() );
                }
            }
                                                               );

        ( (ValueStateModel) stateModel ).manyAssociations().forEach(
            model ->
            {
                try
                {
                    model.checkAssociationConstraints( state.manyAssociationFor( model.accessor() ) );
                }
                catch( ConstraintViolationException e )
                {
                    violations.addAll( e.constraintViolations() );
                }
            }
                                                                   );

        ( (ValueStateModel) stateModel ).namedAssociations().forEach(
            model ->
            {
                try
                {
                    model.checkAssociationConstraints( state.namedAssociationFor( model.accessor() ) );
                }
                catch( ConstraintViolationException e )
                {
                    violations.addAll( e.constraintViolations() );
                }
            }
                                                                    );
        if( !violations.isEmpty() )
        {
            ConstraintViolationException exception = new ConstraintViolationException( violations );
            exception.setCompositeDescriptor( this );
            exception.setIdentity( extractIdentity( state, exception ) );
            throw exception;
        }
    }

    private Identity extractIdentity( ValueStateInstance state, ConstraintViolationException e )
    {
        try
        {
            PropertyInstance<Identity> identityProperty = state.propertyFor( HasIdentity.IDENTITY_METHOD );
            return identityProperty.get();
        }
        catch( IllegalArgumentException e1 )
        {
            // ignore. is not a HasIdentity value
        }
        return null;
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

    private void checkAssociationVisibility()
    {
        // All referenced entity types in any Associations must be visible from the module of this ValueModel.
        TypeLookup lookup = module.typeLookup();
        ValueStateModel stateModel = (ValueStateModel) this.stateModel;
        stateModel.associations().forEach( model -> checkModel( lookup, model ) );
        stateModel.manyAssociations().forEach( model -> checkModel( lookup, model ) );
        stateModel.namedAssociations().forEach( model -> checkModel( lookup, model ) );
    }

    private void checkModel( TypeLookup lookup, AssociationDescriptor model )
    {
        Class<?> rawClass = Classes.RAW_CLASS.apply( model.type() );
        List<EntityDescriptor> descriptors = lookup.lookupEntityModels( rawClass );
        if( descriptors.size() == 0 )
        {
            throw new NoSuchEntityTypeException( rawClass.getName(), module );
        }
    }
}