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

package org.qi4j.runtime.injection.provider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.composite.InvalidValueCompositeException;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.concern.internal.ConcernFor;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.sideeffect.internal.SideEffectFor;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

import static org.qi4j.functional.Iterables.first;

/**
 * JAVADOC
 */
public final class InjectionProviderFactoryStrategy
    implements InjectionProviderFactory
{
    private final Map<Class<? extends Annotation>, InjectionProviderFactory> generalProviderFactories = new HashMap<Class<? extends Annotation>, InjectionProviderFactory>();
    private final Map<Class<? extends Annotation>, InjectionProviderFactory> valuesProviderFactories = new HashMap<Class<? extends Annotation>, InjectionProviderFactory>();
    private MetaInfo metaInfo;

    public InjectionProviderFactoryStrategy( MetaInfo metaInfo )
    {
        this.metaInfo = metaInfo;
        valuesProviderFactories.put( This.class, new ThisInjectionProviderFactory() );
        ModifiesInjectionProviderFactory modifiesInjectionProviderFactory = new ModifiesInjectionProviderFactory();
        valuesProviderFactories.put( ConcernFor.class, modifiesInjectionProviderFactory );
        valuesProviderFactories.put( SideEffectFor.class, modifiesInjectionProviderFactory );
        valuesProviderFactories.put( State.class, new StateInjectionProviderFactory() );

        valuesProviderFactories.put( Structure.class, new CachingInjectionProviderFactoryDecorator( new StructureInjectionProviderFactory() ) );
        valuesProviderFactories.put( Service.class, new CachingInjectionProviderFactoryDecorator( new ServiceInjectionProviderFactory() ) );
        generalProviderFactories.put( Invocation.class, new InvocationInjectionProviderFactory() );
        generalProviderFactories.put( Uses.class, new UsesInjectionProviderFactory() );
    }

    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        Class<? extends Annotation> injectionAnnotationType = dependencyModel.injectionAnnotation().annotationType();
        InjectionProviderFactory factory1 = generalProviderFactories.get( injectionAnnotationType );
        InjectionProviderFactory factory2 = valuesProviderFactories.get( injectionAnnotationType );
        if( factory1 == null && factory2 == null )
        {
            InjectionProviderFactory factory = metaInfo.get( InjectionProviderFactory.class );
            if( factory != null )
            {
                return factory.newInjectionProvider( resolution, dependencyModel );
            }
            else
            {
                throw new InvalidInjectionException( "Unknown injection annotation @" + injectionAnnotationType.getSimpleName() );
            }
        }
        ModelDescriptor composite = resolution.model();
        Class<?> compositeType = first( composite.types() );
        if( factory1 != null && ValueComposite.class.isAssignableFrom( compositeType ) )
        {
            throw new InvalidValueCompositeException( "@" + injectionAnnotationType.getSimpleName() + " is not allowed in ValueComposites: " + compositeType );
        }

        InjectionProviderFactory factory;
        if( factory1 == null )
        {
            factory = factory2;
        }
        else
        {
            factory = factory1;
        }
        return factory.newInjectionProvider( resolution, dependencyModel );
    }
}
