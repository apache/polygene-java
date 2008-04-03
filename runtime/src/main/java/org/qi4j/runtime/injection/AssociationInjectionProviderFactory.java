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

package org.qi4j.runtime.injection;

import java.lang.reflect.Method;
import org.qi4j.composite.State;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.spi.composite.StateResolution;
import org.qi4j.spi.entity.association.AssociationResolution;
import org.qi4j.spi.injection.AssociationInjectionModel;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.StateInjectionContext;

/**
 * TODO
 */
public final class AssociationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        if( resolution.getInjectionModel().getRawInjectionType().equals( State.class ) )
        {
            return new AssociationInjectionProviderFactory.StateInjectionProvider();
        }
        else
        {
            // @AssociationField Association<String> name;
            StateResolution injectable = (StateResolution) bindingContext.getAbstractResolution();
            AssociationInjectionModel aim = (AssociationInjectionModel) resolution.getInjectionModel();
            AssociationResolution associationResolution = injectable.getAssociationResolution( aim.getName() );

            // No such association found
            if( associationResolution == null )
            {
                return null;
            }

            return new AssociationInjectionProviderFactory.AssociationInjectionProvider( associationResolution.getAssociationModel().getAccessor() );
        }
    }

    private class AssociationInjectionProvider implements InjectionProvider
    {
        private Method accessor;

        public AssociationInjectionProvider( Method accessor )
        {
            this.accessor = accessor;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof StateInjectionContext )
            {
                StateInjectionContext associationInjectionContext = (StateInjectionContext) context;
                AbstractAssociation abstractAssociation = associationInjectionContext.getState().getAssociation( accessor );
                if( abstractAssociation != null )
                {
                    return abstractAssociation;
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional association " + accessor.getName() + " had no association" );
                }
            }

            return null;
        }
    }

    private class StateInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof StateInjectionContext )
            {
                StateInjectionContext associationInjectionContext = (StateInjectionContext) context;
                return associationInjectionContext.getState();
            }

            return null;
        }
    }
}
