/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.property.Immutable;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.bootstrap.TransientAssembly;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.property.PropertiesModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Declaration of a TransientComposite.
 */
public final class TransientAssemblyImpl
    extends CompositeAssemblyImpl
    implements TransientAssembly
{
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public TransientAssemblyImpl( Class<?> compositeType )
    {
        this.compositeType = compositeType;
    }

    @Override
    public Class<?> type()
    {
        return compositeType;
    }

    TransientModel newTransientModel(
            StateDeclarations stateDeclarations,
            AssemblyHelper helper
    )
    {
        this.stateDeclarations = stateDeclarations;
        try
        {
            this.helper = helper;

            metaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, metaInfo );

            immutable = metaInfo.get( Immutable.class ) != null;
            propertiesModel = new PropertiesModel();
            stateModel = new StateModel( propertiesModel );
            mixinsModel = new MixinsModel();

            compositeMethodsModel = new CompositeMethodsModel( mixinsModel );

            // The composite must always implement TransientComposite, as a marker interface
            if (!TransientComposite.class.isAssignableFrom(compositeType))
            {
                types.add( TransientComposite.class );
            }

            // If type is a class, register it as a mixin
            if (!compositeType.isInterface())
                mixins.add( compositeType );

            // Implement composite methods
            Iterable<Class<? extends Constraint<?, ?>>> constraintClasses = constraintDeclarations( compositeType );
            Iterable<Class<?>> concernClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( concerns, concernDeclarations( compositeType ) );
            Iterable<Class<?>> sideEffectClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffects, sideEffectDeclarations( compositeType ) );
            Iterable<Class<?>> mixinClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( mixins, mixinDeclarations( compositeType ) );
            implementMixinType( compositeType,
                    constraintClasses, concernClasses, sideEffectClasses, mixinClasses);

            // Implement additional type methods
            for( Class<?> type : types )
            {
                Iterable<Class<? extends Constraint<?, ?>>> typeConstraintClasses = Iterables.<Class<? extends Constraint<?, ?>>, Iterable<Class<? extends Constraint<?, ?>>>>flatten( constraintClasses, constraintDeclarations( type ) );
                Iterable<Class<?>> typeConcernClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( concernClasses, concernDeclarations( type ) );
                Iterable<Class<?>> typeSideEffectClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffectClasses, sideEffectDeclarations( type ) );
                Iterable<Class<?>> typeMixinClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( mixinClasses, mixinDeclarations( type ) );

                implementMixinType( type,
                        typeConstraintClasses, typeConcernClasses, typeSideEffectClasses, typeMixinClasses);
            }

            // Add state from methods and fields
            addState(constraintClasses);

            TransientModel transientModel = new TransientModel(
                compositeType, Iterables.prepend(compositeType, types), visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

            return transientModel;
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }
}
