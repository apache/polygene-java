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
package org.qi4j.runtime.bootstrap;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.ServiceAssembly;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.service.ServiceModel;

/**
 * Assembly of a Service.
 */
public final class ServiceAssemblyImpl extends CompositeAssemblyImpl
    implements ServiceAssembly
{
    String identity;
    boolean instantiateOnStartup = false;
    List<Class<? extends Activator<?>>> activators = new ArrayList<Class<? extends Activator<?>>>();

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

    ServiceModel newServiceModel( StateDeclarations stateDeclarations, AssemblyHelper helper )
    {
        try
        {
            buildComposite( helper, stateDeclarations );
            return new ServiceModel( types, visibility, metaInfo,
                                     new ActivatorsModel( activators ),
                                     mixinsModel, stateModel, compositeMethodsModel,
                                     identity, instantiateOnStartup );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }
}
