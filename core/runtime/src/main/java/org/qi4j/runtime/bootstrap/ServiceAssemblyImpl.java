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
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.bootstrap.ServiceAssembly;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.service.ServiceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembly of a Service.
 */
public final class ServiceAssemblyImpl
    extends CompositeAssemblyImpl
    implements ServiceAssembly
{
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    String identity;
    boolean instantiateOnStartup = false;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public ServiceAssemblyImpl( Class<?> serviceType
    )
    {
        this.compositeType = serviceType;
    }

    @Override
    public Class<?> type()
    {
        return compositeType;
    }

   @Override
   public String identity()
   {
      return identity;
   }

    ServiceModel newServiceModel(
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

            // The composite must always implement ServiceComposite, as a marker interface
            if (!ServiceComposite.class.isAssignableFrom(compositeType))
            {
                types.add( ServiceComposite.class );
            }

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

            ServiceModel serviceModel = new ServiceModel(
                compositeType, Iterables.prepend(compositeType, types), visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel, identity, instantiateOnStartup );

            return serviceModel;
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }
}
