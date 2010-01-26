package org.qi4j.runtime.injection.provider;

import java.io.Serializable;
import org.qi4j.api.entity.association.AbstractAssociation;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.composite.TransientDescriptor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.entity.EntityStateDescriptor;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.spi.entity.association.ManyAssociationDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * JAVADOC
 */
public final class StateInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
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
            if( !( resolution.object() instanceof EntityDescriptor ) )
            {
                throw new InvalidInjectionException( "Only EntityComposites can be injected with '@State UnitOfWork'" );
            }
            return new UnitOfWorkInjectionProvider();
        }
        else if( Property.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @State Property<String> name;
            StateDescriptor descriptor;
            if( resolution.object() instanceof TransientDescriptor )
            {
                descriptor = ( (TransientDescriptor) resolution.object() ).state();
            }
            else if( resolution.object() instanceof ValueDescriptor )
            {
                descriptor = ( (ValueDescriptor) resolution.object() ).state();
            }
            else if( resolution.object() instanceof ServiceDescriptor )
            {
                descriptor = ( (ServiceDescriptor) resolution.object() ).state();
            }
            else
            {
                descriptor = ( (EntityDescriptor) resolution.object() ).state();
            }

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

            PropertyDescriptor propertyDescriptor = descriptor.getPropertyByName( name );

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
            EntityStateDescriptor descriptor = ( (EntityDescriptor) resolution.object() ).state();
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
            EntityStateDescriptor descriptor = ( (EntityDescriptor) resolution.object() ).state();
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
            ManyAssociationDescriptor model = descriptor.getManyAssociationByName( name );

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
        implements InjectionProvider, Serializable
    {
        private final PropertyDescriptor propertyDescriptor;

        public PropertyInjectionProvider( PropertyDescriptor propertyDescriptor )
        {
            this.propertyDescriptor = propertyDescriptor;
        }

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Property value = context.state().getProperty( propertyDescriptor.accessor() );
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
        implements InjectionProvider, Serializable
    {
        private final AssociationDescriptor associationDescriptor;

        public AssociationInjectionProvider( AssociationDescriptor associationDescriptor )
        {
            this.associationDescriptor = associationDescriptor;
        }

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            AbstractAssociation abstractAssociation = ( (EntityStateHolder) context.state() ).getAssociation( associationDescriptor.accessor() );
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
        implements InjectionProvider, Serializable
    {
        private final ManyAssociationDescriptor manyAssociationDescriptor;

        public ManyAssociationInjectionProvider( ManyAssociationDescriptor manyAssociationDescriptor )
        {
            this.manyAssociationDescriptor = manyAssociationDescriptor;
        }

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ManyAssociation abstractAssociation = ( (EntityStateHolder) context.state() ).getManyAssociation( manyAssociationDescriptor.accessor() );
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
        implements InjectionProvider, Serializable
    {
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return context.state();
        }
    }

    static private class UnitOfWorkInjectionProvider
        implements InjectionProvider, Serializable
    {

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return ( (EntityInstance) context.compositeInstance() ).unitOfWork();
        }
    }
}