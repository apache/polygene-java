package org.qi4j.runtime.injection.provider;

import org.qi4j.composite.State;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.spi.property.PropertyDescriptor;

/**
 * TODO
 */
public final class PropertyInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        if( dependencyModel.rawInjectionType().equals( State.class ) )
        {
            // @PropertyField State properties;
            return new StateInjectionProvider();
        }
        else if( Property.class.isAssignableFrom( dependencyModel.rawInjectionType() ) )
        {
            // @PropertyField Property<String> name;
            PropertyDescriptor propertyDescriptor = resolution.composite().state().getPropertyByName( resolution.field().getName() );

            // No such property found
            if( propertyDescriptor == null )
            {
                return null;
            }

            return new PropertyInjectionProvider( propertyDescriptor );
        }

        throw new InjectionProviderException( "Injected value has invalid type" );
    }

    private class PropertyInjectionProvider implements InjectionProvider
    {
        private PropertyDescriptor propertyDescriptor;

        public PropertyInjectionProvider( PropertyDescriptor propertyDescriptor )
        {
            this.propertyDescriptor = propertyDescriptor;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
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

    private class StateInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            return context.state();
        }
    }
}