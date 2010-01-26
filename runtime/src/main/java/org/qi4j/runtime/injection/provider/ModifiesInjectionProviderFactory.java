package org.qi4j.runtime.injection.provider;

import java.io.Serializable;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.spi.composite.AbstractCompositeDescriptor;

/**
 * JAVADOC
 */
public final class ModifiesInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution bindingContext, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        if( bindingContext.object() instanceof AbstractCompositeDescriptor )
        {
            if( dependencyModel.injectionClass().isAssignableFrom( dependencyModel.injectedClass() ) )
            {
                return new ModifiedInjectionProvider();
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + bindingContext.object()
                    .type() + " does not implement @ConcernFor type " + dependencyModel.injectionClass()
                    .getName() + " in modifier " + dependencyModel.injectedClass().getName() );
            }
        }
        else
        {
            throw new InvalidInjectionException( "The class " + dependencyModel.injectedClass()
                .getName() + " is not a modifier" );
        }
    }

    private class ModifiedInjectionProvider
        implements InjectionProvider, Serializable
    {
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return context.next();
        }
    }
}