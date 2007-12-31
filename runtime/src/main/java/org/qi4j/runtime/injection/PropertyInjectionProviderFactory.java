package org.qi4j.runtime.injection;

import java.util.Map;
import org.qi4j.property.AbstractProperty;
import org.qi4j.property.ReadableProperty;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.property.PropertyModel;
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
            return new PropertyMapInjectionProvider();
        }
        else if( AbstractProperty.class.isAssignableFrom( resolution.getInjectionModel().getRawInjectionType() ) )
        {
            MixinResolution mixinResolution = (MixinResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyModel propertyModel = mixinResolution.getPropertyModel( pim.getName() );

            return new PropertyInjectionProvider( propertyModel.getQualifiedName() );
        }
        else
        {
            MixinResolution mixinResolution = (MixinResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyModel propertyModel = mixinResolution.getPropertyModel( pim.getName() );

            return new PropertyValueInjectionProvider( propertyModel.getQualifiedName() );
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
                ReadableProperty value = (ReadableProperty) propertyInjectionContext.getProperties().get( qualifiedName );
                if( value != null )
                {
                    return value.get();
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional property " + qualifiedName + " had no value when" );
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
                AbstractProperty value = propertyInjectionContext.getProperties().get( qualifiedName );
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