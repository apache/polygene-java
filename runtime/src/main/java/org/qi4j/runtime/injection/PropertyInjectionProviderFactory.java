package org.qi4j.runtime.injection;

import java.lang.reflect.Method;
import org.qi4j.composite.State;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.composite.StateResolution;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.PropertyInjectionModel;
import org.qi4j.spi.injection.StateInjectionContext;

/**
 * TODO
 */
public final class PropertyInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        if( resolution.getInjectionModel().getRawInjectionType().equals( State.class ) )
        {
            // @PropertyField State properties;
            return new StateInjectionProvider();
        }
        else if( Property.class.isAssignableFrom( resolution.getInjectionModel().getRawInjectionType() ) )
        {
            // @PropertyField Property<String> name;
            StateResolution injectable = (StateResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyResolution propertyResolution = injectable.getPropertyResolution( pim.getName() );

            // No such property found
            if( propertyResolution == null )
            {
                return null;
            }

            return new PropertyInjectionProvider( propertyResolution.getPropertyModel().getAccessor() );
        }
        else
        {
            StateResolution injectable = (StateResolution) bindingContext.getAbstractResolution();
            PropertyInjectionModel pim = (PropertyInjectionModel) resolution.getInjectionModel();
            PropertyResolution propertyResolution = injectable.getPropertyResolution( pim.getName() );

            return new PropertyValueInjectionProvider( propertyResolution.getPropertyModel().getAccessor() );
        }
    }

    private class PropertyValueInjectionProvider implements InjectionProvider
    {
        private Method accessor;

        public PropertyValueInjectionProvider( Method accessor )
        {
            this.accessor = accessor;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof StateInjectionContext )
            {
                StateInjectionContext stateInjectionContext = (StateInjectionContext) context;
                Property value = stateInjectionContext.getState().getProperty( accessor );
                if( value != null )
                {
                    return value.get();
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional property " + accessor.getName() + " had no value" );
                }
            }

            return null;
        }
    }

    private class PropertyInjectionProvider implements InjectionProvider
    {
        private Method accessor;

        public PropertyInjectionProvider( Method accessor )
        {
            this.accessor = accessor;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof StateInjectionContext )
            {
                StateInjectionContext stateInjectionContext = (StateInjectionContext) context;
                Property value = stateInjectionContext.getState().getProperty( accessor );
                if( value != null )
                {
                    return value;
                }
                else
                {
                    throw new InjectionProviderException( "Non-optional property " + accessor + " had no value" );
                }
            }

            return null;
        }
    }

    private class StateInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof StateInjectionContext )
            {
                StateInjectionContext stateInjectionContext = (StateInjectionContext) context;
                return stateInjectionContext.getState();
            }

            return null;
        }
    }
}