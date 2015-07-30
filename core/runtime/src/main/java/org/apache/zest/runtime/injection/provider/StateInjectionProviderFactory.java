/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.injection.provider;

import org.apache.zest.api.association.AbstractAssociation;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateDescriptor;
import org.apache.zest.api.association.AssociationStateHolder;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.composite.StateDescriptor;
import org.apache.zest.api.composite.StatefulCompositeDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.injection.scope.State;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.property.StateHolder;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.InvalidInjectionException;
import org.apache.zest.runtime.entity.EntityInstance;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.injection.InjectionProvider;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class StateInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( StateHolder.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State StateHolder properties;
            return new StateInjectionProvider();
        }
        else if( UnitOfWork.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            if( !( resolution.model() instanceof EntityDescriptor ) )
            {
                throw new InvalidInjectionException( "Only EntityComposites can be injected with '@State UnitOfWork'" );
            }
            return new UnitOfWorkInjectionProvider();
        }
        else if( Property.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State Property<String> name;
            StateDescriptor descriptor;
            descriptor = ( (StatefulCompositeDescriptor) resolution.model() ).state();

            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().isEmpty() )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }

            PropertyDescriptor propertyDescriptor = descriptor.findPropertyModelByName( name );
            return new PropertyInjectionProvider( propertyDescriptor );
        }
        else if( Association.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State Association<MyEntity> name;
            AssociationStateDescriptor descriptor = ( (EntityDescriptor) resolution.model() ).state();
            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().isEmpty() )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }
            AssociationDescriptor model = descriptor.getAssociationByName( name );
            return new AssociationInjectionProvider( model );
        }
        else if( ManyAssociation.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State ManyAssociation<MyEntity> name;
            AssociationStateDescriptor descriptor = ( (EntityDescriptor) resolution.model() ).state();
            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().isEmpty() )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }
            AssociationDescriptor model = descriptor.getManyAssociationByName( name );
            return new ManyAssociationInjectionProvider( model );
        }
        else if( NamedAssociation.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State NamedAssociation<MyEntity> name;
            AssociationStateDescriptor descriptor = ( (EntityDescriptor) resolution.model() ).state();
            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().isEmpty() )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }
            AssociationDescriptor model = descriptor.getNamedAssociationByName( name );
            return new NamedAssociationInjectionProvider( model );
        }

        throw new InjectionProviderException( "Injected value has invalid type" );
    }

    private static class PropertyInjectionProvider
        implements InjectionProvider
    {
        private final PropertyDescriptor propertyDescriptor;

        private PropertyInjectionProvider( PropertyDescriptor propertyDescriptor )
        {
            this.propertyDescriptor = propertyDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Property<?> value = context.state().propertyFor( propertyDescriptor.accessor() );
            if( value != null )
            {
                return value;
            }
            else
            {
                throw new InjectionProviderException( "Non-optional property " + propertyDescriptor + " had no value" );
            }
        }
    }

    private static class AssociationInjectionProvider
        implements InjectionProvider
    {
        private final AssociationDescriptor associationDescriptor;

        private AssociationInjectionProvider( AssociationDescriptor associationDescriptor )
        {
            this.associationDescriptor = associationDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            AbstractAssociation abstractAssociation = ( (AssociationStateHolder) context.state() ).
                associationFor( associationDescriptor.accessor() );
            if( abstractAssociation != null )
            {
                return abstractAssociation;
            }
            else
            {
                throw new InjectionProviderException( "Non-optional association " + associationDescriptor.qualifiedName() + " had no association" );
            }
        }
    }

    private static class ManyAssociationInjectionProvider
        implements InjectionProvider
    {
        private final AssociationDescriptor manyAssociationDescriptor;

        private ManyAssociationInjectionProvider( AssociationDescriptor manyAssociationDescriptor )
        {
            this.manyAssociationDescriptor = manyAssociationDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ManyAssociation<?> abstractAssociation = ( (AssociationStateHolder) context.state() ).
                manyAssociationFor( manyAssociationDescriptor.accessor() );
            if( abstractAssociation != null )
            {
                return abstractAssociation;
            }
            else
            {
                throw new InjectionProviderException( "Non-optional association " + manyAssociationDescriptor.qualifiedName() + " had no association" );
            }
        }
    }

    private static class NamedAssociationInjectionProvider
        implements InjectionProvider
    {
        private final AssociationDescriptor namedAssociationDescriptor;

        private NamedAssociationInjectionProvider( AssociationDescriptor namedAssociationDescriptor )
        {
            this.namedAssociationDescriptor = namedAssociationDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            NamedAssociation<?> abstractAssociation = ( (AssociationStateHolder) context.state() ).
                namedAssociationFor( namedAssociationDescriptor.accessor() );
            if( abstractAssociation != null )
            {
                return abstractAssociation;
            }
            else
            {
                throw new InjectionProviderException( "Non-optional association " + namedAssociationDescriptor.qualifiedName() + " had no association" );
            }
        }
    }

    static private class StateInjectionProvider
        implements InjectionProvider
    {
        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return context.state();
        }
    }

    static private class UnitOfWorkInjectionProvider
        implements InjectionProvider
    {

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return ( (EntityInstance) context.compositeInstance() ).unitOfWork();
        }
    }

}
