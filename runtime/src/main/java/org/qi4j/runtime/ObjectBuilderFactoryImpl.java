/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.ObjectBuilder;
import org.qi4j.api.ObjectBuilderFactory;
import org.qi4j.api.annotation.scope.Adapt;
import org.qi4j.api.annotation.scope.Decorate;
import org.qi4j.api.annotation.scope.PropertyField;
import org.qi4j.api.annotation.scope.PropertyParameter;
import org.qi4j.api.model.ObjectModel;
import org.qi4j.runtime.resolution.AdaptDependencyResolver;
import org.qi4j.runtime.resolution.DecorateDependencyResolver;
import org.qi4j.runtime.resolution.DependencyResolverDelegator;
import org.qi4j.runtime.resolution.ObjectModelResolver;
import org.qi4j.runtime.resolution.ObjectResolution;
import org.qi4j.runtime.resolution.PropertyDependencyResolver;
import org.qi4j.spi.dependency.DependencyResolver;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * Default implementation of ObjectBuilderFactory
 */
public final class ObjectBuilderFactoryImpl
    implements ObjectBuilderFactory
{
    private Map<Class, ObjectResolution> objectResolutions;
    private ObjectModelFactory modelFactory;
    private InstanceFactory instanceFactory;
    private ObjectModelResolver objectModelResolver;

    public ObjectBuilderFactoryImpl()
    {
        DependencyResolverDelegator dependencyResolverDelegator = new DependencyResolverDelegator();

        dependencyResolverDelegator.setDependencyResolver( Adapt.class, new AdaptDependencyResolver() );
        dependencyResolverDelegator.setDependencyResolver( Decorate.class, new DecorateDependencyResolver() );
        PropertyDependencyResolver dependencyResolver = new PropertyDependencyResolver();
        dependencyResolverDelegator.setDependencyResolver( PropertyField.class, dependencyResolver );
        dependencyResolverDelegator.setDependencyResolver( PropertyParameter.class, dependencyResolver );

        init( dependencyResolverDelegator );
    }

    public ObjectBuilderFactoryImpl( DependencyResolver resolver )
    {
        init( resolver );
    }

    public <T> ObjectBuilder<T> newObjectBuilder( Class<T> type )
    {
        try
        {
            ObjectResolution<T> resolution = getObjectResolution( type );
            ObjectBuilder<T> builder = new ObjectBuilderImpl<T>( resolution, instanceFactory );
            return builder;
        }
        catch( InvalidDependencyException e )
        {
            throw new CompositeInstantiationException( "Could not resolve dependencies", e );
        }
    }

    // Private ------------------------------------------------------
    private void init( DependencyResolver resolver )
    {
        modelFactory = new ObjectModelFactory();

        objectModelResolver = new ObjectModelResolver( resolver );

        objectResolutions = new ConcurrentHashMap<Class, ObjectResolution>();
        instanceFactory = new InstanceFactoryImpl();
    }

    private <T> ObjectResolution<T> getObjectResolution( Class<T> type )
        throws InvalidDependencyException
    {
        ObjectResolution<T> objectResolution = objectResolutions.get( type );
        if( objectResolution == null )
        {
            ObjectModel<T> model = modelFactory.newObjectModel( type );
            objectResolution = objectModelResolver.resolveModel( model );
            objectResolutions.put( type, objectResolution );
        }
        return objectResolution;
    }
}