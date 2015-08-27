/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
package org.apache.zest.runtime.bootstrap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.zest.api.activation.Activator;
import org.apache.zest.api.activation.Activators;
import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.util.Annotations;
import org.apache.zest.api.util.Classes;
import org.apache.zest.bootstrap.ServiceAssembly;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.functional.Iterables;
import org.apache.zest.runtime.activation.ActivatorsModel;
import org.apache.zest.runtime.service.ServiceModel;

/**
 * Assembly of a Service.
 */
public final class ServiceAssemblyImpl extends CompositeAssemblyImpl
    implements ServiceAssembly
{
    String identity;
    boolean instantiateOnStartup = false;
    List<Class<? extends Activator<?>>> activators = new ArrayList<>();

    public ServiceAssemblyImpl( Class<?> serviceType )
    {
        super( serviceType );
        // The composite must always implement ServiceComposite, as a marker interface
        if( !ServiceComposite.class.isAssignableFrom( serviceType ) )
        {
            types.add( ServiceComposite.class );
        }
    }

    @Override
    public String identity()
    {
        return identity;
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    ServiceModel newServiceModel( StateDeclarations stateDeclarations, AssemblyHelper helper )
    {
        try
        {
            buildComposite( helper, stateDeclarations );
            List<Class<? extends Activator<?>>> activatorClasses = Iterables.toList(
                Iterables.<Class<? extends Activator<?>>>flatten( activators, activatorsDeclarations( types.stream() ) ) );
            return new ServiceModel( types, visibility, metaInfo,
                                     new ActivatorsModel( activatorClasses ),
                                     mixinsModel, stateModel, compositeMethodsModel,
                                     identity, instantiateOnStartup );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }

    private Iterable<Class<? extends Activator<?>>> activatorsDeclarations( Stream<? extends Class<?>> typess )
    {
        return typess.flatMap( Classes::typesOf )
//            .filter( type -> Annotations.annotationOn( type, Activators.class ) == null )
            .flatMap( this::getAnnotations )
            .collect( Collectors.toList() );
    }

    private Stream<? extends Class<? extends Activator<?>>> getAnnotations( Type type )
    {
        Activators activators = Annotations.annotationOn( type, Activators.class );
        if( activators == null )
        {
            return Stream.empty();
        }
        return Arrays.stream( activators.value() );
    }


}
