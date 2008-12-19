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

package org.qi4j.runtime.injection.provider;

import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.AssociationField;
import org.qi4j.api.injection.scope.AssociationParameter;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.EntityStateDescriptor;

/**
 * TODO
 */
public final class AssociationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( dependencyModel.rawInjectionType().equals( EntityStateHolder.class ) )
        {                                               
            return new AssociationInjectionProviderFactory.StateInjectionProvider();
        }
        else
        {
            AssociationDescriptor model;

            EntityStateDescriptor descriptor = (EntityStateDescriptor) resolution.composite().state();
            if( dependencyModel.injectionAnnotation().annotationType().equals( AssociationField.class ) )
            {
                // @AssociationField Association<String> name;
                model = descriptor.getAssociationByName( resolution.field().getName() );

            }
            else
            {
                AssociationParameter param = (AssociationParameter) dependencyModel.injectionAnnotation();
                model = descriptor.getAssociationByName( param.value() );
            }

            // No such association found
            if( model == null )
            {
                return null;
            }

            return new AssociationInjectionProviderFactory.AssociationInjectionProvider( model );
        }
    }

    private class AssociationInjectionProvider implements InjectionProvider
    {
        private final AssociationDescriptor associationDescriptor;

        public AssociationInjectionProvider( AssociationDescriptor associationDescriptor )
        {
            this.associationDescriptor = associationDescriptor;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            AbstractAssociation abstractAssociation = ((EntityStateHolder) context.state()).getAssociation( associationDescriptor.accessor() );
            if( abstractAssociation != null )
            {
                return abstractAssociation;
            }
            else
            {
                throw new InjectionProviderException( "Non-optional association " + associationDescriptor.name() + " had no association" );
            }
        }
    }

    private class StateInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            return context.state();
        }
    }
}
