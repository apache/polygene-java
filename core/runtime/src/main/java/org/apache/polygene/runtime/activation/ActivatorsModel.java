/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.runtime.activation;

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;

/**
 * Activators Model.
 *
 * @param <ActivateeType> Type of the activation target
 */
public class ActivatorsModel<ActivateeType>
    implements VisitableHierarchy<Object, Object>
{

    private final List<ActivatorModel<ActivateeType>> activatorModels = new ArrayList<>();
    private final Iterable<Class<? extends Activator<ActivateeType>>> activatorsClasses;

    public ActivatorsModel( Iterable<Class<? extends Activator<ActivateeType>>> activatorsClasses )
    {
        this.activatorsClasses = activatorsClasses;
        for( Class<? extends Activator<ActivateeType>> activatorClass : activatorsClasses )
        {
            activatorModels.add( new ActivatorModel<>( activatorClass ) );
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ActivatorModel<ActivateeType> activatorModel : activatorModels )
            {
                if( !activatorModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public Iterable<ActivatorModel<ActivateeType>> models()
    {
        return activatorModels;
    }

    public Iterable<Activator<ActivateeType>> newInstances()
        throws ActivationException
    {
        List<Activator<ActivateeType>> activators = new ArrayList<>();
        for( ActivatorModel<ActivateeType> activatorModel : activatorModels )
        {
            activators.add( activatorModel.newInstance() );
        }
        return activators;
    }

    public Iterable<Activator<ActivateeType>> newInstances( ModuleDescriptor module )
        throws ActivationException
    {
        List<Activator<ActivateeType>> activators = new ArrayList<>();
        for( ActivatorModel<ActivateeType> activatorModel : activatorModels )
        {
            InjectionContext injectionContext = new InjectionContext( module, UsesInstance.EMPTY_USES );
            activators.add( activatorModel.newInstance( injectionContext ) );
        }
        return activators;
    }

}
