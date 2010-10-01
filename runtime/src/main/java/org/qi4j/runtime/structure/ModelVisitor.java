/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import org.qi4j.runtime.composite.AbstractConstraintModel;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.composite.ConstructorModel;
import org.qi4j.runtime.composite.MethodConcernModel;
import org.qi4j.runtime.composite.MethodConcernsModel;
import org.qi4j.runtime.composite.MethodConstraintsModel;
import org.qi4j.runtime.composite.MethodSideEffectModel;
import org.qi4j.runtime.composite.MethodSideEffectsModel;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.injection.InjectedMethodModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.value.ValueModel;

/**
 * Visitor interface for traversing the internal model. You can specify an exception
 * that you want to be able to throw from the visit methods to abort the traversal. Use
 * RuntimeException if you don't have any other specific exception you want to throw.
 */
public class ModelVisitor<ThrowableType extends Exception>
{
    public void visit( ApplicationModel applicationModel )
        throws ThrowableType
    {
    }

    public void visit( LayerModel layerModel )
        throws ThrowableType
    {
    }

    public void visit( ModuleModel moduleModel )
        throws ThrowableType
    {
    }

    public void visit( TransientModel transientModel )
        throws ThrowableType
    {
    }

    public void visit( ValueModel valueModel )
        throws ThrowableType
    {
    }

    public void visit( CompositeMethodModel compositeMethodModel )
        throws ThrowableType
    {
    }

    public void visit( MethodConstraintsModel methodConstraintsModel )
        throws ThrowableType
    {
    }

    public void visit( AbstractConstraintModel constraintModel )
        throws ThrowableType
    {
    }

    public void visit( MethodConcernsModel methodConcernsModel )
        throws ThrowableType
    {
    }

    public void visit( MethodConcernModel methodConcernModel )
        throws ThrowableType
    {
    }

    public void visit( MethodSideEffectsModel methodSideEffectsModel )
        throws ThrowableType
    {
    }

    public void visit( MethodSideEffectModel methodSideEffectModel )
        throws ThrowableType
    {
    }

    public void visit( ConstructorModel constructorModel )
        throws ThrowableType
    {
    }

    public void visit( InjectedParametersModel injectedParametersModel )
        throws ThrowableType
    {
    }

    public void visit( InjectedFieldModel injectedFieldModel )
        throws ThrowableType
    {
    }

    public void visit( InjectedMethodModel injectedMethodModel )
        throws ThrowableType
    {
    }

    public void visit( MixinModel mixinModel )
        throws ThrowableType
    {
    }

    public void visit( EntityModel entityModel )
        throws ThrowableType
    {
    }

    public void visit( ServiceModel serviceModel )
        throws ThrowableType
    {
    }

    public void visit( ImportedServiceModel serviceModel )
        throws ThrowableType
    {
    }

    public void visit( ObjectModel objectModel )
        throws ThrowableType
    {
    }
}
