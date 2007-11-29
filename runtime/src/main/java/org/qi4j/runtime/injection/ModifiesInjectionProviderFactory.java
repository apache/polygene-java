package org.qi4j.runtime.injection;

import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionModel;
import org.qi4j.spi.dependency.InjectionProvider;
import org.qi4j.spi.dependency.InjectionProviderFactory;
import org.qi4j.spi.dependency.InjectionResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.dependency.ModifierInjectionContext;

/**
 * TODO
 */
public class ModifiesInjectionProviderFactory
    implements InjectionProviderFactory
{

    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        InjectionModel injectionModel = resolution.getInjectionModel();
        if( resolution.getCompositeModel() != null )
        {
            if( injectionModel.getInjectionClass().isAssignableFrom( injectionModel.getInjectedClass() ) )
            {
                return new ModifiedInjectionProvider();
            }
            else
            {
                throw new InvalidInjectionException( "Composite " + resolution.getCompositeModel().getCompositeClass() + " does not implement @ConcernFor type " + injectionModel.getInjectionClass() + " in modifier " + injectionModel.getInjectedClass() );
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