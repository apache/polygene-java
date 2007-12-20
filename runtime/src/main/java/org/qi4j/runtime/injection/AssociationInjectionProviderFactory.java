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

package org.qi4j.runtime.injection;

import java.lang.reflect.Method;
import java.util.Map;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.spi.injection.AssociationInjectionContext;
import org.qi4j.spi.injection.AssociationInjectionModel;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public class AssociationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        if( resolution.getInjectionModel().getInjectedClass().equals( Method.class ) && resolution.getInjectionModel().getRawInjectionType().equals( Map.class ) )
        {
            return new AssociationInjectionProviderFactory.AssociationMapInjectionProvider();
        }
        else
        {
            return new AssociationInjectionProviderFactory.AssociationInjectionProvider( resolution );
        }
    }

    private class AssociationInjectionProvider implements InjectionProvider
    {
        private InjectionResolution association;
        private String name;

        public AssociationInjectionProvider( InjectionResolution association )
        {
            this.association = association;
            name = ( (AssociationInjectionModel) association.getInjectionModel() ).getName();
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof AssociationInjectionContext )
            {
                AssociationInjectionContext associationInjectionContext = (AssociationInjectionContext) context;
                AbstractAssociation abstractAssociation = associationInjectionContext.getAssociations().get( name );
                if( abstractAssociation != null )
                {
                    return abstractAssociation;
                }
                else
                {
                    if( this.association.getInjectionModel().isOptional() )
                    {
                        return null;
                    }
                    else
                    {
                        throw new InjectionProviderException( "Non-optional association " + name + " had no association when injecting " + this.association.getAbstractModel().getModelClass().getName() );
                    }
                }
            }

            return null;
        }
    }

    private class AssociationMapInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof AssociationInjectionContext )
            {
                AssociationInjectionContext associationInjectionContext = (AssociationInjectionContext) context;
                return associationInjectionContext.getAssociations();
            }

            return null;
        }
    }
}
