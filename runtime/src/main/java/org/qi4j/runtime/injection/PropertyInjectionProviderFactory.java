package org.qi4j.runtime.injection;

import java.util.Map;
import org.qi4j.property.Property;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.composite.StateResolution;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.PropertyInjectionContext;
import org.qi4j.spi.injection.PropertyInjectionModel;

/**
 * TODO
 */
public final class PropertyInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        if( resolution.getInjectionModel().getInjectionClass().equals( String.class ) && resolution.getInjectionModel().getRawInjectionType().equals( Map.class ) )
        {
            // @PropertyField Map<String, Property> properties;
            return new PropertyMapInjectionProvider();
        }
        else if( Property.class.isAssignableFrom( resolution.getInjectionModel().getRawInjectionType() ) )
        {
            // @PropertyField Property<String> name;
            StateResolution injectable = (StateResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyResolution propertyResolution = injectable.getPropertyResolution( pim.getName() );

            return new PropertyInjectionProvider( propertyResolution.getPropertyModel().getQualifiedName() );
        }
        else
        {
            StateResolution injectable = (StateResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyResolution propertyResolution = injectable.getPropertyResolution( pim.getName() );

            return new PropertyValueInjectionProvider( propertyResolution.getPropertyModel().getQualifiedName() );
        }
    }

    private class PropertyValueInjectionProvider implements InjectionProvider
    {
        private String qualifiedName;

        public PropertyValueInjectionProvider( String qualifiedName )
        {
            this.qualifiedName = qualifiedName;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof PropertyInjectionContext )
            {
                PropertyInjectionContext propertyInjectionContext = (PropertyInjectionContext) context;
                ImmutableProperty value = (ImmutableProperty) propertyInjectionContext.getProperties().get( qualifiedName );
                if( value != null )
                {
                    return value.get();
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional property " + qualifiedName + " had no value" );
                }
            }

            return null;
        }
    }

    private class PropertyInjectionProvider implements InjectionProvider
    {
        private String qualifiedName;

        public PropertyInjectionProvider( String qualifiedName )
        {
            this.qualifiedName = qualifiedName;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof PropertyInjectionContext )
            {
                PropertyInjectionContext propertyInjectionContext = (PropertyInjectionContext) context;
                Property value = propertyInjectionContext.getProperties().get( qualifiedName );
                if( value != null )
                {
                    return value;
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional property " + qualifiedName + " had no value when" );
                }
            }

            return null;
        }
    }

    private class PropertyMapInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof PropertyInjectionContext )
            {
                PropertyInjectionContext propertyInjectionContext = (PropertyInjectionContext) context;
                return propertyInjectionContext.getProperties();
            }

            return null;
        }
    }
}