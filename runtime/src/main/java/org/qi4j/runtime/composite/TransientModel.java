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

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.StateHolder;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.TransientDescriptor;

/**
 * Model for Transient Composites
 */
public class TransientModel
    extends AbstractCompositeModel
    implements TransientDescriptor
{
    public static TransientModel newModel( final Class<? extends Composite> compositeType,
                                           final Visibility visibility,
                                           final MetaInfo metaInfo,
                                           final PropertyDeclarations propertyDeclarations,
                                           final Iterable<Class<?>> assemblyConcerns,
                                           final Iterable<Class<?>> sideEffects, List<Class<?>> mixins
    )
    {
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        PropertiesModel propertiesModel = new PropertiesModel( constraintsModel, propertyDeclarations, immutable );
        StateModel stateModel = new StateModel( propertiesModel );
        MixinsModel mixinsModel = new MixinsModel( compositeType, mixins );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( assemblyConcerns, concerns );
        ConcernsDeclaration.concernDeclarations( compositeType, concerns );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( concerns );

        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType, sideEffects );
        CompositeMethodsModel compositeMethodsModel =
            new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel );
        stateModel.addStateFor( compositeMethodsModel.methods(), compositeType );

        return new TransientModel(
            compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    protected TransientModel( final Class<? extends Composite> compositeType,
                              final Visibility visibility,
                              final MetaInfo metaInfo,
                              final MixinsModel mixinsModel,
                              final StateModel stateModel,
                              final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositeMethodsModel.visitModel( modelVisitor );
        mixinsModel.visitModel( modelVisitor );
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
    }

    public Composite newProxy( InvocationHandler invocationHandler )
        throws ConstructionException
    {
        // Instantiate proxy for given composite interface
        try
        {
            return Composite.class.cast( proxyClass.getConstructor( InvocationHandler.class ).newInstance( invocationHandler ) );
        }
        catch( Exception e )
        {
            throw new ConstructionException( e );
        }
    }

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance,
                                                   UsesInstance uses,
                                                   StateHolder state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        CompositeInstance compositeInstance = new TransientInstance( this, moduleInstance, mixins, state );

        try
        {
            // Instantiate all mixins
            ( (MixinsModel) mixinsModel ).newMixins( compositeInstance,
                                                     uses,
                                                     state,
                                                     mixins );
        }
        catch( InvalidCompositeException e )
        {
            e.setFailingCompositeType( type() );
            e.setMessage( "Invalid Cyclic Mixin usage dependency" );
            throw e;
        }

        stateModel.setComputedProperties( state, compositeInstance );

        // Return
        return compositeInstance;
    }

    public StateHolder newBuilderState()
    {
        return stateModel.newBuilderInstance();
    }

    public StateHolder newState( StateHolder state )
    {
        return stateModel.newInstance( state );
    }

    @Override
    public String toString()
    {
        return type().getName();
    }
}