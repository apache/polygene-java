package org.qi4j.runtime.injection.provider;

import org.qi4j.composite.State;
import org.qi4j.injection.scope.PropertyField;
import org.qi4j.injection.scope.PropertyParameter;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.spi.composite.StateDescriptor;
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
            StateDescriptor descriptor = resolution.composite().state();
            PropertyDescriptor propertyDescriptor = null;
            if( dependencyModel.injectionAnnotation().annotationType().equals( PropertyField.class ) )
            {
                PropertyField annotation = (PropertyField) dependencyModel.injectionAnnotation();
                String name;
                if( annotation.value().equals( "" ) )
                {
                    name = resolution.field().getName();
                }
                else
                {
                    name = annotation.value();
                }
                propertyDescriptor = descriptor.getPropertyByName( name );
            }
            else
            {
                PropertyParameter annotation = (PropertyParameter) dependencyModel.injectionAnnotation();
                propertyDescriptor = descriptor.getPropertyByName( annotation.value() );
            }

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
        private final PropertyDescriptor propertyDescriptor;

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