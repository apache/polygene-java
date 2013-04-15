package org.qi4j.runtime.injection.provider;

import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.association.AssociationStateDescriptor;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.StateDescriptor;
import org.qi4j.api.composite.StatefulCompositeDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

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
            if( annotation.value().equals( "" ) )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }

            PropertyDescriptor propertyDescriptor = descriptor.findPropertyModelByName( name );

            // Check if property exists
            if( propertyDescriptor == null )
            {
                return null;
            }

            return new PropertyInjectionProvider( propertyDescriptor );
        }
        else if( Association.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State Association<MyEntity> name;
            AssociationStateDescriptor descriptor = ( (EntityDescriptor) resolution.model() ).state();
            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().equals( "" ) )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }
            AssociationDescriptor model = descriptor.getAssociationByName( name );

            // No such association found
            if( model == null )
            {
                return null;
            }

            return new AssociationInjectionProvider( model );
        }
        else if( ManyAssociation.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State ManyAssociation<MyEntity> name;
            AssociationStateDescriptor descriptor = ( (EntityDescriptor) resolution.model() ).state();
            State annotation = (State) dependencyModel.injectionAnnotation();
            String name;
            if( annotation.value().equals( "" ) )
            {
                name = resolution.field().getName();
            }
            else
            {
                name = annotation.value();
            }
            AssociationDescriptor model = descriptor.getManyAssociationByName( name );

            // No such association found
            if( model == null )
            {
                return null;
            }

            return new ManyAssociationInjectionProvider( model );
        }

        throw new InjectionProviderException( "Injected value has invalid type" );
    }

    static private class PropertyInjectionProvider
        implements InjectionProvider
    {
        private final PropertyDescriptor propertyDescriptor;

        public PropertyInjectionProvider( PropertyDescriptor propertyDescriptor )
        {
            this.propertyDescriptor = propertyDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Property value = context.state().propertyFor( propertyDescriptor.accessor() );
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

    static private class AssociationInjectionProvider
        implements InjectionProvider
    {
        private final AssociationDescriptor associationDescriptor;

        public AssociationInjectionProvider( AssociationDescriptor associationDescriptor )
        {
            this.associationDescriptor = associationDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            AbstractAssociation abstractAssociation = ( (AssociationStateHolder) context.state() ).associationFor( associationDescriptor
                                                                                                                       .accessor() );
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

    static private class ManyAssociationInjectionProvider
        implements InjectionProvider
    {
        private final AssociationDescriptor manyAssociationDescriptor;

        public ManyAssociationInjectionProvider( AssociationDescriptor manyAssociationDescriptor )
        {
            this.manyAssociationDescriptor = manyAssociationDescriptor;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ManyAssociation abstractAssociation = ( (AssociationStateHolder) context.state() ).manyAssociationFor( manyAssociationDescriptor
                                                                                                                       .accessor() );
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