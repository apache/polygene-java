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
import org.qi4j.composite.internal.ConcernFor;
import org.qi4j.composite.internal.SideEffectFor;
import org.qi4j.injection.scope.Invocation;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Uses;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;

/**
 * TODO
 */
public final class InjectionProviderFactoryStrategy
    implements InjectionProviderFactory
{
    private Map<Class<? extends Annotation>, InjectionProviderFactory> providerFactories = new HashMap<Class<? extends Annotation>, InjectionProviderFactory>();

    public InjectionProviderFactoryStrategy()
    {
        providerFactories.put( This.class, new ThisInjectionProviderFactory() );
        ModifiesInjectionProviderFactory modifiesInjectionProviderFactory = new ModifiesInjectionProviderFactory();
        providerFactories.put( ConcernFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( SideEffectFor.class, modifiesInjectionProviderFactory );
        providerFactories.put( Invocation.class, new InvocationInjectionProviderFactory() );
        providerFactories.put( Uses.class, new UsesInjectionProviderFactory() );
        PropertyInjectionProviderFactory propertyInjectionProviderFactory = new PropertyInjectionProviderFactory();
//        providerFactories.put( PropertyField.class, propertyInjectionProviderFactory );
//        providerFactories.put( PropertyParameter.class, propertyInjectionProviderFactory );
        AssociationInjectionProviderFactory associationInjectionProviderFactory = new AssociationInjectionProviderFactory();
//        providerFactories.put( AssociationField.class, associationInjectionProviderFactory );
//        providerFactories.put( AssociationParameter.class, associationInjectionProviderFactory );
        providerFactories.put( Structure.class, new CachingInjectionProviderFactoryDecorator( new StructureInjectionProviderFactory() ) );
        providerFactories.put( Service.class, new CachingInjectionProviderFactoryDecorator( new ServiceInjectionProviderFactory() ) );
    }

    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        Class<? extends Annotation> injectionAnnotationType = dependencyModel.injectionAnnotation().annotationType();
        InjectionProviderFactory factory = providerFactories.get( injectionAnnotationType );
        if( factory == null )
        {
            throw new InvalidInjectionException( "Unknown injection annotation @" + injectionAnnotationType.getSimpleName() );
        }

        return factory.newInjectionProvider( resolution, dependencyModel );
    }
}
