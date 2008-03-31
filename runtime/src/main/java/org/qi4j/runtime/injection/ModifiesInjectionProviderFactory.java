package org.qi4j.runtime.injection;

import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionModel;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.ModifierInjectionContext;

/**
 * TODO
 */
public final class ModifiesInjectionProviderFactory
    implements InjectionProviderFactory
{

    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        InjectionModel injectionModel = resolution.getInjectionModel();
        if( bindingContext.getCompositeResolution() != null )
        {
            if( injectionModel.getInjectionClass().isAssignableFrom( injectionModel.getInjectedClass() ) )
            {
                return new ModifiedInjectionProvider();
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + bindingContext.getCompositeResolution().getCompositeModel().getCompositeType() + " does not implement @ConcernFor type " + injectionModel.getInjectionClass().getName() + " in modifier " + injectionModel.getInjectedClass() );
            }
        }
        else
        {
            throw new InvalidInjectionException( "The class " + injectionModel.getInjectedClass().getName() + " is not a modifier" );
        }
    }

    private class ModifiedInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context )
        {
            return ( (ModifierInjectionContext) context ).getModifies();
        }
    }
}