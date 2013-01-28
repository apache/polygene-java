/*
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
package org.qi4j.runtime.activation;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.Activator;

public class ActivatorsModel<T>
{

    private final Iterable<Class<? extends Activator<T>>> activatorsClasses;

    public ActivatorsModel( Iterable<Class<? extends Activator<T>>> activatorsClasses )
    {
        this.activatorsClasses = activatorsClasses;
    }

    public Iterable<Activator<T>> newInstances()
        throws ActivationException
    {
        List<Activator<T>> activators = new ArrayList<Activator<T>>();
        for ( Class<? extends Activator<T>> activatorClass : activatorsClasses ) {
            try
            {
                activators.add( activatorClass.newInstance() );
            }
            catch( Exception e )
            {
                throw new ActivationException( "Unable to instantiate " + activatorClass, e  );
            }
        }
        return activators;
    }

}
