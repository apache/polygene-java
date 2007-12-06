/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

import org.qi4j.spi.composite.BindingException;
import org.qi4j.spi.composite.ConstructorBinding;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.MethodBinding;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public class ObjectBinder
    extends CompositeBinder
{
    public ObjectBinder( InjectionProviderFactory injectionProviderFactory )
    {
        super( injectionProviderFactory );
    }

    public ObjectBinding bindObject( ObjectResolution objectResolution )
    {
        try
        {
            ConstructorResolution constructorResolution = objectResolution.getConstructorResolutions().iterator().next(); // TODO Pick the best one
            ConstructorBinding constructorBinding = bindConstructor( constructorResolution );
            Iterable<FieldBinding> fieldBindings = bindFields( objectResolution.getFieldResolutions() );
            Iterable<MethodBinding> methodBindings = bindMethods( objectResolution.getMethodResolutions() );
            ObjectBinding objectBinding = new ObjectBinding( objectResolution, constructorBinding, fieldBindings, methodBindings );
            return objectBinding;
        }
        catch( InvalidInjectionException e )
        {
            throw new BindingException( "Could not bind injections", e );
        }
    }
}
