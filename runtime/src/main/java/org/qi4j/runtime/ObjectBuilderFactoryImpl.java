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
import org.qi4j.CompositeInstantiationException;
import org.qi4j.ObjectBuilder;
import org.qi4j.ObjectBuilderFactory;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.model.ObjectModel;
import org.qi4j.runtime.resolution.ObjectModelResolver;
import org.qi4j.runtime.resolution.ObjectResolution;

/**
 * Default implementation of ObjectBuilderFactory
 */
public final class ObjectBuilderFactoryImpl
    implements ObjectBuilderFactory
{
    private Map<Class, ObjectResolution> objectResolutions;
    private ObjectModelFactory objectModelFactory;
    private InstanceFactory instanceFactory;
    private ObjectModelResolver objectModelResolver;

    public ObjectBuilderFactoryImpl( InstanceFactory instanceFactory, ObjectModelFactory objectModelFactory, ObjectModelResolver objectModelResolver )
    {
        this.objectModelFactory = objectModelFactory;
        this.instanceFactory = instanceFactory;
        this.objectModelResolver = objectModelResolver;

        objectResolutions = new ConcurrentHashMap<Class, ObjectResolution>();
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
    private <T> ObjectResolution<T> getObjectResolution( Class<T> type )
        throws InvalidDependencyException
    {
        ObjectResolution<T> objectResolution = objectResolutions.get( type );
        if( objectResolution == null )
        {
            ObjectModel<T> model = objectModelFactory.newObjectModel( type );
            objectResolution = objectModelResolver.resolveModel( model );
            objectResolutions.put( type, objectResolution );
        }
        return objectResolution;
    }
}