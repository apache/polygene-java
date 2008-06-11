package org.qi4j.runtime.injection.provider;

import org.qi4j.runtime.composite.qi.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;

/**
 * TODO
 */
public final class ModifiesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        if( bindingContext.composite() != null )
        {
            if( dependencyModel.injectionClass().isAssignableFrom( dependencyModel.injectedClass() ) )
            {
                return new ModifiedInjectionProvider();
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + bindingContext.composite().type() + " does not implement @ConcernFor type " + dependencyModel.injectionClass().getName() + " in modifier " + dependencyModel.injectedClass().getName() );
            }
        }
        else
        {
            throw new InvalidInjectionException( "The class " + dependencyModel.injectedClass().getName() + " is not a modifier" );
        }
    }

    private class ModifiedInjectionProvider implements InjectionProvider
    {
        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            return context.next();
        }
    }
}