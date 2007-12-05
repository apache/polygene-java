package org.qi4j.runtime.injection;

import org.qi4j.composite.PropertyValue;
import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionProvider;
import org.qi4j.spi.dependency.InjectionProviderException;
import org.qi4j.spi.dependency.InjectionProviderFactory;
import org.qi4j.spi.dependency.InjectionResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.dependency.PropertyInjectionContext;
import org.qi4j.spi.dependency.PropertyInjectionModel;

/**
 * TODO
 */
public class PropertyInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        if( resolution.getInjectionModel().getInjectedClass().equals( PropertyValue.class ) && resolution.getInjectionModel().getRawInjectionType().equals( Iterable.class ) )
        {
            return new PropertySetInjectionProvider();
        }
        else
        {
            return new PropertyInjectionProvider( resolution );
        }
    }

    private class PropertyInjectionProvider implements InjectionProvider
    {
        private InjectionResolution property;
        private String name;

        public PropertyInjectionProvider( InjectionResolution property )
        {
            this.property = property;
            name = ( (PropertyInjectionModel) property.getInjectionModel() ).getName();
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof PropertyInjectionContext )
            {
                PropertyInjectionContext propertyInjectionContext = (PropertyInjectionContext) context;
                PropertyValue value = propertyInjectionContext.getProperties().get( name );
                if( value != null )
                {
                    return value.getValue();
                }
                else
                {
                    if( property.getInjectionModel().isOptional() )
                    {
                        return null;
                    }
                    else
                    {
                        throw new InjectionProviderException( "Non-optional property " + name + " had no value when injecting " + property.getAbstractModel().getModelClass().getName() );
                    }
                }
            }

            return null;
        }
    }

    private class PropertySetInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof PropertyInjectionContext )
            {
                PropertyInjectionContext propertyInjectionContext = (PropertyInjectionContext) context;
                return propertyInjectionContext.getProperties().values();
            }

            return null;
        }
    }
}