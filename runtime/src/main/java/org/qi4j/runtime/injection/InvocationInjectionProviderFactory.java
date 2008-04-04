package org.qi4j.runtime.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.ModifierInjectionContext;

/**
 * TODO
 */
public final class InvocationInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        Class injectionClass = resolution.getInjectionModel().getInjectionClass();
        if( injectionClass.equals( Method.class ) ||
            injectionClass.equals( AnnotatedElement.class ) ||
            Annotation.class.isAssignableFrom( injectionClass ) )
        {
            return new InvocationDependencyResolution( resolution );
        }
        else
        {
            throw new InvalidInjectionException( "Invalid injection type " + injectionClass + " in " + resolution.getInjectionModel().getInjectedClass().getName() );
        }
    }

    private class InvocationDependencyResolution implements InjectionProvider
    {
        private InjectionResolution resolution;

        public InvocationDependencyResolution( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            ModifierInjectionContext modifierContext = (ModifierInjectionContext) context;
            Class injectedClass = resolution.getInjectionModel().getInjectionClass();
            if( injectedClass.equals( Method.class ) )
            {
                // This needs to be updated to handle Apply and annotation aggregation correctly
                return modifierContext.getMethod().getCompositeMethodResolution().getCompositeMethodModel().getMethod();
            }
            else if( injectedClass.equals( AnnotatedElement.class ) )
            {
                return modifierContext.getMethod().getCompositeMethodResolution().getAnnotatedElement();
            }
            else
            {
                return modifierContext.getMethod().getCompositeMethodResolution().getAnnotatedElement().getAnnotation( injectedClass );
            }
        }
    }
}