/*
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.runtime.activation;

import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.Activator;
import org.apache.zest.api.structure.Module;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;
import org.apache.zest.runtime.composite.UsesInstance;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.structure.ModuleInstance;

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

    public Iterable<Activator<ActivateeType>> newInstances( Module module )
        throws ActivationException
    {
        List<Activator<ActivateeType>> activators = new ArrayList<>();
        for( ActivatorModel<ActivateeType> activatorModel : activatorModels )
        {
            InjectionContext injectionContext = new InjectionContext( (ModuleInstance) module, UsesInstance.EMPTY_USES );
            activators.add( activatorModel.newInstance( injectionContext ) );
        }
        return activators;
    }

}
